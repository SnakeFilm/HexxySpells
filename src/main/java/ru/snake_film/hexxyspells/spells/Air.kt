package ru.snake_film.hexxyspells.spells

import io.redspace.ironsspellbooks.api.registry.SchoolRegistry
import io.redspace.ironsspellbooks.entity.spells.AbstractMagicProjectile
import io.redspace.ironsspellbooks.entity.spells.fireball.MagicFireball
import io.redspace.ironsspellbooks.entity.spells.magma_ball.FireBomb
import io.redspace.ironsspellbooks.registries.EntityRegistry
import io.redspace.ironsspellbooks.registries.SoundRegistry
import io.redspace.ironsspellbooks.util.ParticleHelper
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.projectile.Projectile
import net.minecraft.world.entity.projectile.ProjectileUtil
import net.minecraft.world.level.ClipContext
import net.minecraft.world.level.Level
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3
import ru.snake_film.hexxyspells.spells.utils_and_entities.BeamUtils
import ru.snake_film.hexxyspells.spells.utils_and_entities.ChannelingEffectEntity

object AirElement : IElementalSchool {
    override fun getIronSchool() = SchoolRegistry.LIGHTNING.get()

    override fun getManaCost(power: Double, index: Int) = (power * 25f).toFloat()
    override fun getMediaCost(power: Double, index: Int) = (power * 5000L).toLong()

    override fun createProjectile(index: Int, world: Level, caster: LivingEntity, damage: Double): Projectile? {
        val dmg = damage.toFloat()*1.5.toFloat()
        return when (index) {
            1 -> setup(EntityRegistry.LIGHTNING_LANCE_PROJECTILE.get().create(world), caster, dmg)
            2 -> setup(EntityRegistry.BALL_LIGHTNING.get().create(world), caster, dmg)
            else -> null
        }}

    override fun createPointEffect(index: Int, world: Level, caster: LivingEntity, pos: Vec3, damage: Double) {
        if (world.isClientSide || world !is ServerLevel) return
        val dmg = damage.toFloat()

        when (index) {
            1 -> { // Грозовой Удар (Lightning Strike)
                val strike = io.redspace.ironsspellbooks.registries.EntityRegistry.LIGHTNING_STRIKE.get().create(world)
                if (strike != null) {
                    strike.owner = caster
                    strike.damage = dmg
                    strike.setPos(pos.x, pos.y, pos.z)
                    world.addFreshEntity(strike)
                }
            }
            2 -> { // Эпицентр Шоквейва (Расталкивающий импульс ветра)
                val shockwave = io.redspace.ironsspellbooks.registries.EntityRegistry.GUST_COLLIDER.get().create(world)
                if (shockwave != null) {
                    shockwave.owner = caster
                    shockwave.setPos(pos.x, pos.y, pos.z)
                    world.addFreshEntity(shockwave)
                }
            }
        }
    }
    override fun createInstantEffect(index: Int, world: Level, caster: LivingEntity, dir: Vec3, power: Double) {
        if (world.isClientSide || world !is ServerLevel) return
        val startPos = caster.eyePosition
        val normalizedDir = dir.normalize()

        when (index) {
            1 -> { // ААРД / GUST
                val gust = io.redspace.ironsspellbooks.registries.EntityRegistry.GUST_COLLIDER.get().create(world)
                if (gust != null) {
                    gust.owner = caster
                    // Устанавливаем позицию чуть впереди кастера
                    val spawnPos = caster.eyePosition.add(normalizedDir.scale(1.5))
                    gust.setPos(spawnPos.x, spawnPos.y - 0.5, spawnPos.z)

                    // Задаем движение колайдеру Ирона
                    gust.deltaMovement = normalizedDir.scale(0.8)
                    //gust.setRange((5.0 + power).toFloat())

                    world.addFreshEntity(gust)
                    world.playSound(null, caster.blockPosition(), io.redspace.ironsspellbooks.registries.SoundRegistry.SHOCKWAVE_CAST.get(), SoundSource.PLAYERS, 1.0f, 1.2f)
                }
            }

            2 -> { // VOLT STRIKE / CHAIN LIGHTNING
                val maxRange = 16.0 + power
                // Используем ProjectileUtil или ванильный поиск сущностей по вектору взгляда
                val lookVec = caster.lookAngle
                val startPos = caster.eyePosition
                val endPos = startPos.add(lookVec.scale(maxRange))
                val boundingBox = caster.boundingBox.expandTowards(lookVec.scale(maxRange)).inflate(1.0)

                // Ищем живую цель в векторе взгляда
                val entityHit = net.minecraft.world.entity.projectile.ProjectileUtil.getEntityHitResult(
                    caster, startPos, endPos, boundingBox, { it is LivingEntity && !it.isAlliedTo(caster) }, maxRange * maxRange
                )

                if (entityHit != null && entityHit.entity is LivingEntity) {
                    val targetEntity = entityHit.entity as LivingEntity
                    val chainLightning = io.redspace.ironsspellbooks.registries.EntityRegistry.CHAIN_LIGHTNING.get().create(world)

                    if (chainLightning != null) {
                        chainLightning.owner = caster
                        chainLightning.damage = (6.0 * power).toFloat()
                        // Важно: привязываем к позиции РЕАЛЬНОЙ цели, а не воздуха
                        chainLightning.setPos(targetEntity.x, targetEntity.y + targetEntity.bbHeight / 2.0, targetEntity.z)

                        world.addFreshEntity(chainLightning)
                        world.playSound(null, caster.blockPosition(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 0.5f, 1.5f)
                    }
                } else {
                    // Если никого не нашли, вместо краша спавним обычную точечную молнию под ноги в точку взгляда
                    val hitResult = caster.pick(maxRange, 1.0f, false)
                    val targetPos = hitResult.location
                    val lightning = io.redspace.ironsspellbooks.registries.EntityRegistry.LIGHTNING_STRIKE.get().create(world)
                    if (lightning != null) {
                        lightning.owner = caster
                        lightning.damage = (4.0 * power).toFloat()
                        lightning.setPos(targetPos.x, targetPos.y, targetPos.z)
                        world.addFreshEntity(lightning)
                    }
                }
            }}}
    override fun createContinuousEffect(index: Int, world: Level, caster: LivingEntity, direction: Vec3, power: Double, duration: Int) {
        val entity = ChannelingEffectEntity(world, caster, "air", power, duration, index, BeamUtils.BeamType.CONE)

        when (index) {
            1 -> { // Aero Burst
                entity.behavior = BeamUtils.BeamType.CONE

                    //entity.setParticleType(ParticleTypes.SWEEP_ATTACK)
                entity.setSoundEvent(SoundRegistry.GUST_CAST.get())
            }
            2 -> { // Vacuum Link (Луч)
                entity.behavior = BeamUtils.BeamType.BEAM
                entity.setParticleType(ParticleTypes.INSTANT_EFFECT) // Мелкие белые искры
                entity.setSoundEvent(SoundRegistry.TELEKINESIS_LOOP.get())
            }
            3 -> { // Cyclone Pull
                entity.behavior = BeamUtils.BeamType.CONE
                entity.setParticleType(ParticleTypes.CLOUD)
                entity.setSoundEvent(SoundRegistry.GUST_CHARGE.get())
            }
            4 -> { // Electrocute (Конус молний)
                entity.behavior = BeamUtils.BeamType.CONE
                entity.setParticleType(ParticleHelper.ELECTRICITY)

                entity.setSoundEvent(SoundRegistry.ELECTROCUTE_LOOP.get())
            }
            5 -> { // Thunderbolt (Луч-разряд)
                entity.behavior = BeamUtils.BeamType.BEAM
                entity.setParticleType(ParticleHelper.ELECTRICITY)
                // Используем визуальный луч, он выглядит как тонкая линия тока
                entity.setVisualEntityType(EntityRegistry.RAY_OF_FROST_VISUAL_ENTITY.get())
                entity.setSoundEvent(SoundRegistry.LIGHTNING_LANCE_CAST.get())
            }
            else -> return
        }

        entity.setPos(caster.x, caster.y, caster.z)
        world.addFreshEntity(entity)
    }

    override fun getBehavior(index: Int) = when(index) {
        1 -> BeamUtils.BeamType.CONE // Breath
        2 -> BeamUtils.BeamType.BEAM // Ray
        else -> BeamUtils.BeamType.BEAM
    }
    override fun performBeamHit(world: Level, caster: LivingEntity, target: Entity, power: Double) {
        target.hurt(target.damageSources().magic(), (power * 0.5).toFloat())
    }
    private fun drawInstantLineParticles(world: ServerLevel, start: Vec3, end: Vec3, particle: net.minecraft.core.particles.ParticleOptions, count: Int) {
        for (i in 0..count) {
            val ratio = i / count.toDouble()
            val pos = start.lerp(end, ratio)
            world.sendParticles(particle, pos.x, pos.y, pos.z, 1, 0.0, 0.0, 0.0, 0.0)
        }
    }

    private fun setup(p: Entity?, caster: LivingEntity, dmg: Float): Projectile? {
        if (p is AbstractMagicProjectile) {
            p.owner = caster
            p.damage = dmg
        }
        return p as? Projectile
    }

    private fun spawnEntity(type: EntityType<*>, world: Level, pos: Vec3) {
        type.create(world)?.let {
            it.moveTo(pos.x, pos.y, pos.z)
            world.addFreshEntity(it)
        }
    }
}