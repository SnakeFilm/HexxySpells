package ru.snake_film.hexxyspells.spells

import io.redspace.ironsspellbooks.api.registry.SchoolRegistry
import io.redspace.ironsspellbooks.entity.spells.AbstractMagicProjectile
import io.redspace.ironsspellbooks.entity.spells.fireball.MagicFireball
import io.redspace.ironsspellbooks.entity.spells.ice_block.IceBlockProjectile
import io.redspace.ironsspellbooks.entity.spells.magma_ball.FireBomb
import io.redspace.ironsspellbooks.registries.EntityRegistry
import io.redspace.ironsspellbooks.registries.SoundRegistry
import io.redspace.ironsspellbooks.util.ParticleHelper
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.projectile.Projectile
import net.minecraft.world.entity.projectile.ProjectileUtil
import net.minecraft.world.level.ClipContext
import net.minecraft.world.level.Level
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3
import ru.snake_film.hexxyspells.spells.utils_and_entities.BeamUtils
import ru.snake_film.hexxyspells.spells.utils_and_entities.ChannelingEffectEntity

object WaterElement : IElementalSchool {
    override fun getIronSchool() = SchoolRegistry.ICE.get()

    override fun getManaCost(power: Double, index: Int) = (power * 12f).toFloat()
    override fun getMediaCost(power: Double, index: Int) = (power * 1200L).toLong()

    override fun createProjectile(index: Int, world: Level, caster: LivingEntity, damage: Double): Projectile? {
        val dmg = damage.toFloat()
        return when (index) {

        1 -> setup(EntityRegistry.ICICLE_PROJECTILE.get().create(world), caster, dmg)
        2 -> setup(EntityRegistry.SNOWBALL.get().create(world), caster, dmg)
        3 -> IceBlockProjectile(EntityRegistry.ICE_BLOCK_PROJECTILE.get(), world).apply {
            owner = caster // Устанавливаем владельца отдельно, так как его нет в конструкторе
        }

        4 -> setup(EntityRegistry.RAY_OF_FROST_VISUAL_ENTITY.get().create(world), caster, dmg)
            else -> null
        }


    }



        override fun createContinuousEffect(index: Int, world: Level, caster: LivingEntity, direction: Vec3, power: Double, duration: Int) {
            val entity = ChannelingEffectEntity(world, caster, "water", power, duration, index, BeamUtils.BeamType.CONE)

            when (index) {
                // --- СЕКЦИЯ ВОДЫ ---
                1 -> { // Water Torrent (Поток)
                    entity.behavior = BeamUtils.BeamType.CONE
                    entity.setParticleType(ParticleTypes.SPLASH)
                    entity.setSoundEvent(net.minecraft.sounds.SoundEvents.GENERIC_SPLASH) // Если нет в SoundRegistry, используй ванильный: SoundEvents.GENERIC_SPLASH
                }
                2 -> { // Hydro Jet (Струя)
                    entity.behavior = BeamUtils.BeamType.BEAM
                    entity.setParticleType(ParticleTypes.BUBBLE)
                    entity.setVisualEntityType(EntityRegistry.RAY_OF_FROST_VISUAL_ENTITY.get())
                    entity.setSoundEvent(net.minecraft.sounds.SoundEvents.GENERIC_SPLASH)
                }
                3 -> { // Tsunami Shield (Водный щит - сбивает снаряды)
                    entity.behavior = BeamUtils.BeamType.CONE
                    entity.setParticleType(ParticleTypes.DRIPPING_WATER)
                    entity.setSoundEvent(net.minecraft.sounds.SoundEvents.GENERIC_SPLASH)    // Звук порыва ветра/воды
                }

                // --- СЕКЦИЯ ЛЬДА (МОРОЗ) ---
                4 -> { // Cone of Cold (Классика из списка)
                    entity.behavior = BeamUtils.BeamType.CONE
                    entity.setParticleType(ParticleTypes.SNOWFLAKE)
                    entity.setSoundEvent(SoundRegistry.CONE_OF_COLD_LOOP.get())
                }
                5 -> { // Ray of Frost (Замораживающий луч)
                    entity.behavior = BeamUtils.BeamType.BEAM
                    entity.setParticleType(ParticleTypes.SNOWFLAKE)
                    entity.setVisualEntityType(EntityRegistry.RAY_OF_FROST_VISUAL_ENTITY.get())
                    entity.setSoundEvent(SoundRegistry.RAY_OF_FROST.get())
                }
                else -> return
            }

            entity.setPos(caster.x, caster.y, caster.z)
            world.addFreshEntity(entity)

    }



override fun createPointEffect(index: Int, world: Level, caster: LivingEntity, pos: Vec3, damage: Double)
{}
    override fun createInstantEffect(index: Int, world: Level, caster: LivingEntity, dir: Vec3, power: Double) {
        if (world.isClientSide || world !is ServerLevel) return
        val startPos = caster.eyePosition
        val normalizedDir = dir.normalize()

        when (index) {
            1 -> { // RAY OF FROST
                val maxRange = 20.0 + power
                val hitResult = caster.pick(maxRange, 1.0f, false)
                val endPos = hitResult.location

                // Создаем родную сущность визуала луча из твоего реестра
                val visualRay =
                    io.redspace.ironsspellbooks.registries.EntityRegistry.RAY_OF_FROST_VISUAL_ENTITY.get().create(world)
                if (visualRay != null) {
                    visualRay.setPos(caster.x, caster.eyeY - 0.3, caster.z)
                    // Нацеливаем сущность луча в точку попадания
                    visualRay.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES, endPos)
                    world.addFreshEntity(visualRay)
                }

                // Нанесение урона сущности, в которую попали
                if (hitResult is net.minecraft.world.phys.EntityHitResult && hitResult.entity is LivingEntity) {
                    val target = hitResult.entity as LivingEntity
                    if (!target.isAlliedTo(caster)) {
                        target.hurt(world.damageSources().magic(), (5.0 * power).toFloat())
                        target.ticksFrozen += (120 * power).toInt()
                    }
                }
                world.playSound(null, caster.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0f, 1.3f)
            }
        }
    }

    override fun getBehavior(index: Int) = when(index) {
        1 -> BeamUtils.BeamType.CONE // Breath
        2 -> BeamUtils.BeamType.BEAM // Ray
        else -> BeamUtils.BeamType.BEAM
    }

    override fun performBeamHit(world: Level, caster: LivingEntity, target: Entity, power: Double) {

        if (target is LivingEntity) {
            target.addEffect(MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 2))
            target.hurt(world.damageSources().freeze(), power.toFloat())
        }
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
