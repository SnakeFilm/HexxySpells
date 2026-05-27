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


object FireElement : IElementalSchool {
    override fun getIronSchool() = SchoolRegistry.FIRE.get()
    override fun getManaCost(power: Double, index: Int) = (power * 8f).toFloat()
    override fun getMediaCost(power: Double, index: Int) = (power * 5000L).toLong()

    override fun createProjectile(index: Int, world: Level, caster: LivingEntity, damage: Double): Projectile? {
        val dmg = damage.toFloat()
        return when (index) {
            1 -> setup(EntityRegistry.FIREBOLT_PROJECTILE.get().create(world), caster, dmg)
            2 -> MagicFireball(world, caster).apply {
                this.damage = dmg
                this.explosionRadius = (dmg / 2f).coerceAtLeast(1f)
            }
            3 -> FireBomb(world, caster).apply {
                this.damage = dmg
                this.explosionRadius = (dmg / 3f).coerceAtLeast(1f)
            }
            4 -> setup(EntityRegistry.FIRE_ARROW_PROJECTILE.get().create(world), caster, dmg)
            5 -> setup(EntityRegistry.FIERY_DAGGER_PROJECTILE.get().create(world), caster, dmg)
            else -> null
        }
    }

    override fun createPointEffect(index: Int, world: Level, caster: LivingEntity, pos: Vec3, damage: Double) {
        val dmg = damage.toFloat()
        when (index) {
            1 -> { // Столб пламени (Sunbeam)
                val sunbeam = io.redspace.ironsspellbooks.registries.EntityRegistry.SUNBEAM.get().create(world)
                if (sunbeam != null) {
                    sunbeam.owner = caster
                    sunbeam.damage = dmg
                    sunbeam.setPos(pos.x, pos.y, pos.z)
                    world.addFreshEntity(sunbeam)
                }
            }
            2 -> spawnEntity(EntityRegistry.FIRE_ERUPTION_AOE.get(), world, pos)
            3 -> spawnEntity(EntityRegistry.WALL_OF_FIRE_ENTITY.get(), world, pos)
        }
    }

    override fun createContinuousEffect(index: Int, world: Level, caster: LivingEntity, direction: Vec3, power: Double, duration: Int) {

        // Создаем маркер под заклинание
        val entity = ChannelingEffectEntity(world, caster, "fire", power, duration, index, BeamUtils.BeamType.CONE)

        // Инициализируем параметры напрямую в зависимости от индекса заклинания
        // Теперь компилятор идеально видит все типы данных (SoundEvent, EntityType и т.д.)
        when (index) {
            1 -> { // Fire Breath
                entity.behavior = BeamUtils.BeamType.CONE
                entity.setParticleType(net.minecraft.core.particles.ParticleTypes.FLAME)
                entity.setVisualEntityType(null)
                entity.setSoundEvent(io.redspace.ironsspellbooks.registries.SoundRegistry.FIRE_BREATH_LOOP.get())
            }
            2 -> { // Твой кастомный луч (например, Frost/Fire Ray)
                entity.behavior = BeamUtils.BeamType.BEAM
                entity.setParticleType(net.minecraft.core.particles.ParticleTypes.SOUL_FIRE_FLAME)
                entity.setVisualEntityType(io.redspace.ironsspellbooks.registries.EntityRegistry.RAY_OF_FROST_VISUAL_ENTITY.get())
                entity.setSoundEvent(io.redspace.ironsspellbooks.registries.SoundRegistry.FIRE_CAST.get())
            }
            3 -> { // Blaze Storm
                entity.behavior = BeamUtils.BeamType.CONE
                entity.setParticleType(net.minecraft.core.particles.ParticleTypes.LAVA)
                entity.setVisualEntityType(null)
                entity.setSoundEvent(io.redspace.ironsspellbooks.registries.SoundRegistry.FIRE_IMPACT.get())
            }
            else -> return // Если индекс не существует, прерываем создание
        }

        // Задаем позицию и спавним в мир
        entity.setPos(caster.x, caster.y, caster.z)
        world.addFreshEntity(entity)
    }

    // Вспомогательный дата-класс для удобной инициализации 4-х параметров
    //private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

    override fun createInstantEffect(index: Int, world: Level, caster: LivingEntity, dir: Vec3, power: Double) {
        if (world.isClientSide || world !is ServerLevel) return
        val startPos = caster.eyePosition
        val normalizedDir = dir.normalize()

        when (index) {

            1 -> {
                val range = 5.0 + power
                val right = normalizedDir.cross(Vec3(0.0, 1.0, 0.0)).normalize()

                // Вместо сотен шагов, делаем редкие сочные импульсы кастомных частиц
                for (i in 1..4) {
                    val dist = i * (range / 4.0)
                    val center = startPos.add(normalizedDir.scale(dist))

                    // Спавним ведьмачьи летящие искры Ирона
                    world.sendParticles(
                        io.redspace.ironsspellbooks.registries.ParticleRegistry.FIRE_PARTICLE.get(),
                        center.x, center.y, center.z,
                        8, dist * 0.2, 0.3, dist * 0.2, 0.05
                    )
                }

                // Наносим урон + честный ПОДЖОГ
                val box = caster.boundingBox.inflate(range)
                world.getEntitiesOfClass(LivingEntity::class.java, box).forEach { target ->
                    if (target != caster && !target.isAlliedTo(caster)) {
                        val toTarget = target.position().subtract(startPos)
                        if (toTarget.length() <= range && toTarget.normalize().dot(normalizedDir) > 0.7) {

                            target.hurt(world.damageSources().onFire(), (6.0 * power).toFloat())
                            // Поджигаем цель на 4 секунды * power
                            target.remainingFireTicks = (80 * power).toInt()

                            // Эффект возгорания Ирона
                            world.sendParticles(ParticleTypes.FLAME, target.x, target.getY(0.5), target.z, 5, 0.1, 0.2, 0.1, 0.02)
                        }
                    }
                }
                world.playSound(null, caster.blockPosition(), SoundEvents.FLINTANDSTEEL_USE, SoundSource.PLAYERS, 1.5f, 0.8f)
            }}}
    override fun getBehavior(index: Int) = when(index) {
        1 -> BeamUtils.BeamType.CONE // Breath
        2 -> BeamUtils.BeamType.BEAM // Ray
        else -> BeamUtils.BeamType.BEAM
    }

    override fun performBeamHit(world: Level, caster: LivingEntity, target: Entity, power: Double) {
        if (target is LivingEntity) {
            target.setSecondsOnFire(5 * power.toInt())
            target.hurt(world.damageSources().magic(), power.toFloat() * 0.5F)
        }
    }
    private fun drawInstantLineParticles(world: ServerLevel, start: Vec3, end: Vec3, particle: net.minecraft.core.particles.ParticleOptions, count: Int) {
        for (i in 0..count) {
            val ratio = i / count.toDouble()
            val pos = start.lerp(end, ratio)
            world.sendParticles(particle, pos.x, pos.y, pos.z, 1, 0.0, 0.0, 0.0, 0.0)
        }
    }


    // Хелперы для чистоты кода
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