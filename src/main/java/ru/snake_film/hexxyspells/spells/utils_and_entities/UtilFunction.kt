package ru.snake_film.hexxyspells.spells.utils_and_entities

import io.redspace.ironsspellbooks.entity.spells.fireball.SmallMagicFireball
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.projectile.Projectile
import net.minecraft.world.entity.projectile.ProjectileUtil
import net.minecraft.world.level.Level
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.phys.Vec3
import ru.snake_film.hexxyspells.spells.AirElement
import ru.snake_film.hexxyspells.spells.IElementalSchool
import ru.snake_film.hexxyspells.spells.SpiritElement
import ru.snake_film.hexxyspells.spells.WaterElement
import thedarkcolour.kotlinforforge.forge.vectorutil.v3d.minus

object BeamUtils {
    enum class BeamType { BEAM, CONE }

    fun cast(
        world: Level,
        caster: LivingEntity,
        dir: Vec3,
        power: Double,
        school: IElementalSchool,
        type: BeamType,
        index: Int
    ) {

        // ==========================================
        // 1. ЛОГИКА ВОЗДУХА (AIR) - Теперь изолирована
        // ==========================================
        if (school == AirElement) {
            when (index) {
                1 -> { // Реактивный прыжок / Отталкивание
                    if (caster.xRot > 60f) {
                        // Исправленный антигравитационный бустер (не отменяет полет)
                        val upPush = 0.12 * power
                        val currentMotion = caster.deltaMovement
                        val newY = if (currentMotion.y < 0) upPush else currentMotion.y + (upPush * 0.5)

                        caster.deltaMovement = Vec3(currentMotion.x * 1.05, newY, currentMotion.z * 1.05)
                        caster.fallDistance = 0f
                        caster.hurtMarked = true

                        if (caster.tickCount % 5 == 0) {
                            world.playSound(null, caster.x, caster.y, caster.z, SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 0.5f, 1.5f)
                        }
                    }
                    handleConePush(world, caster, dir, power, 0.3)
                    return // Выходим, чтобы не шло в базовую логику лучей
                }

                2 -> { // Vacuum Link (Связывание)
                    val hit = ProjectileUtil.getHitResultOnViewVector(caster, { it is LivingEntity && it != caster }, 12.0)
                    if (hit is EntityHitResult) {
                        val target = hit.entity as LivingEntity
                        val desiredPos = caster.eyePosition.add(dir.scale(5.0))
                        val pullVec = desiredPos.subtract(target.position()).scale(0.01 * power)

                        target.deltaMovement = target.deltaMovement.add(pullVec) - target.deltaMovement
                        target.fallDistance = 0f
                        target.hurtMarked = true
                    }
                    return
                }

                3 -> { // ИСПРАВЛЕННЫЙ ЦИКЛОН
                    val targetCenter = caster.eyePosition.add(dir.scale(4.0))
                    val targets = world.getEntitiesOfClass(LivingEntity::class.java, caster.boundingBox.inflate(6.0))
                    for (target in targets) {
                        if (target == caster || target.isAlliedTo(caster)) continue

                        val toTarget = target.position().subtract(caster.eyePosition).normalize()
                        if (toTarget.dot(dir) > 0.6) {
                            val pullDirection = targetCenter.subtract(target.position())
                            val distance = pullDirection.length()

                            if (distance > 0.2) {
                                val pullStrength = 0.15 * power
                                val pullVelocity = pullDirection.normalize().scale(pullStrength)
                                target.deltaMovement = target.deltaMovement.add(pullVelocity.x, 0.05, pullVelocity.z)
                                target.hurtMarked = true
                            }
                        }
                    }
                    return
                }
            }
        }

        // ==========================================
        // 2. ЛОГИКА ВОДЫ (WATER) - Защитный щит
        // ==========================================
        if (school == WaterElement && index == 3) {
            val projectiles = world.getEntitiesOfClass(Projectile::class.java, caster.boundingBox.inflate(5.0))
            for (proj in projectiles) {
                if (proj.owner == caster) continue // Не сбиваем свои снаряды

                val toProj = proj.position().subtract(caster.eyePosition).normalize()
                if (toProj.dot(dir) > 0.7) {
                    if (proj.isOnFire || proj is net.minecraft.world.entity.projectile.LargeFireball || proj is net.minecraft.world.entity.projectile.SmallFireball) {
                        proj.discard()

                        (world as? ServerLevel)?.sendParticles(
                            ParticleTypes.POOF,
                            proj.x, proj.y, proj.z,
                            5, 0.1, 0.1, 0.1, 0.05
                        )

                        world.playSound(null, proj.x, proj.y, proj.z, SoundEvents.FIRE_EXTINGUISH, SoundSource.NEUTRAL, 0.5f, 1.8f)
                    } else {
                        proj.deltaMovement = proj.deltaMovement.scale(0.5).add(dir.scale(0.2))
                    }
                }
            }
        }

        // ==========================================
        // 3. ЛОГИКА ОГНЯ (FIRE) - ОНА ТЕПЕРЬ СРАБОТАЕТ!
        // ==========================================
        if (school.toString().contains("Fire") && index == 3 && type == BeamType.CONE) {
            if (caster.tickCount % 3 == 0) {
                spawnBlazeFireball(world, caster, dir, power)
            }
            return
        }
        // ==========================================
        // 4. СТИХИЯ: ДУХ (SPIRIT)
        // ==========================================
        if (school == SpiritElement || school.toString().contains("Spirit")) {
            when (index) {
                1 -> { // Dragon Breath (Потоковый конус урона)
                    val targets = world.getEntitiesOfClass(LivingEntity::class.java, caster.boundingBox.inflate(5.0))
                    for (target in targets) {
                        if (target == caster || target.isAlliedTo(caster)) continue

                        val toTarget = target.position().subtract(caster.eyePosition).normalize()
                        if (toTarget.dot(dir) > 0.75) {
                            school.performBeamHit(world, caster, target, power)

                            if (world.random.nextFloat() > 0.85f && !world.isClientSide) {
                                val areaEffectCloud = net.minecraft.world.entity.AreaEffectCloud(world, target.x, target.y, target.z)
                                areaEffectCloud.owner = caster
                                areaEffectCloud.setParticle(ParticleTypes.DRAGON_BREATH)
                                areaEffectCloud.radius = 1.5f
                                areaEffectCloud.duration = 60
                                world.addFreshEntity(areaEffectCloud)
                            }
                        }
                    }
                    return
                }

                2 -> {
                    // Sunbeam (Урон полностью обрабатывается внутренним кодом SunbeamEntity)
                    return
                }

                3 -> { // Starfall (Периодический АОЕ-урон в точке прицела)
                    if (caster.tickCount % 4 == 0) {
                        val rayTraceRange = 32.0
                        val hitResult = world.clip(
                            net.minecraft.world.level.ClipContext(
                                caster.eyePosition,
                                caster.eyePosition.add(caster.lookAngle.scale(rayTraceRange)),
                                net.minecraft.world.level.ClipContext.Block.COLLIDER,
                                net.minecraft.world.level.ClipContext.Fluid.NONE,
                                caster
                            )
                        )
                        val targetCenter = hitResult.location

                        // Ищем цели в кубе 12x8x12 (радиус 6 в стороны от центра)
                        val aoeTargets = world.getEntitiesOfClass(LivingEntity::class.java, net.minecraft.world.phys.AABB(
                            targetCenter.x - 6.0, targetCenter.y - 3.0, targetCenter.z - 6.0,
                            targetCenter.x + 6.0, targetCenter.y + 5.0, targetCenter.z + 6.0
                        ))

                        for (target in aoeTargets) {
                            if (target == caster || target.isAlliedTo(caster)) continue
                            school.performBeamHit(world, caster, target, power)
                        }
                    }
                    return
                }
            }
        }

        // ==========================================
        // 5. БАЗОВАЯ ЛОГИКА ДЛЯ ОСТАЛЬНЫХ ЛУЧЕЙ
        // ==========================================
        when (type) {
            BeamType.BEAM -> {
                val hit = ProjectileUtil.getHitResultOnViewVector(caster, { it is LivingEntity && it != caster }, 20.0)
                if (hit is EntityHitResult) {
                    val target = hit.entity as LivingEntity

                    if (!target.isAlliedTo(caster)) {
                        school.performBeamHit(world, caster, target, power)

                        if (school == WaterElement && index == 5) {
                            target.ticksFrozen += 40
                        }
                    }
                }
            }

            BeamType.CONE -> {
                val targets = world.getEntitiesOfClass(LivingEntity::class.java, caster.boundingBox.inflate(5.0))
                for (target in targets) {
                    if (target == caster || target.isAlliedTo(caster)) continue

                    val toTarget = target.position().subtract(caster.eyePosition).normalize()
                    if (toTarget.dot(dir) > 0.8) {
                        school.performBeamHit(world, caster, target, power)

                        // ВОДА (1, 2, 3) - Толкаем
                        if (school == WaterElement && index in 1..3) {
                            target.clearFire()
                            val push = if (index == 3) 0.2 else 0.1
                            target.deltaMovement = target.deltaMovement.add(dir.x * push, 0.1, dir.z * push)
                            target.hurtMarked = true
                        }

                        // МОРОЗ (4) - Замеляем
                        if (school == WaterElement && index == 4) {
                            target.addEffect(net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, 40, 1))
                        }
                    }
                }
            }
        }
    }

    private fun spawnBlazeFireball(world: Level, caster: LivingEntity, dir: Vec3, power: Double) {
        val yaw = Math.toRadians(caster.yRot.toDouble())
        val rightHandVector = Vec3(-Math.cos(yaw), 0.0, -Math.sin(yaw)).normalize()
        val handOffset = rightHandVector.scale(0.35).add(dir.scale(0.4))
        val spawnPos = caster.eyePosition.subtract(0.0, 0.4, 0.0).add(handOffset)

        val fireball = SmallMagicFireball(world, caster)
        fireball.moveTo(spawnPos.x, spawnPos.y, spawnPos.z, caster.yRot, caster.xRot)
        fireball.damage = power.toFloat() * 0.3F

        val speed = fireball.speed.toDouble()
        val velocityVec = dir.normalize().scale(speed)
        fireball.shoot(velocityVec, 0.25F)

        world.addFreshEntity(fireball)
    }

    private fun handleConePush(world: Level, caster: LivingEntity, dir: Vec3, power: Double, strength: Double) {
        val targets = world.getEntitiesOfClass(LivingEntity::class.java, caster.boundingBox.inflate(6.0))
        for (target in targets) {
            if (target == caster || target.isAlliedTo(caster)) continue
            val toTarget = target.position().subtract(caster.eyePosition).normalize()
            if (toTarget.dot(dir) > 0.7) {
                target.deltaMovement = target.deltaMovement.add(dir.x * strength, 0.1, dir.z * strength)
                target.hurtMarked = true
            }
        }
    }
}