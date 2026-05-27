package ru.snake_film.hexxyspells.spells

import io.redspace.ironsspellbooks.api.registry.SchoolRegistry
import io.redspace.ironsspellbooks.entity.spells.AbstractMagicProjectile
import io.redspace.ironsspellbooks.entity.spells.fireball.MagicFireball
import io.redspace.ironsspellbooks.entity.spells.magma_ball.FireBomb
import io.redspace.ironsspellbooks.registries.EntityRegistry
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.projectile.Projectile
import net.minecraft.world.level.Level
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import ru.snake_film.hexxyspells.spells.actions.InstantAction.drawLaserBeam
import ru.snake_film.hexxyspells.spells.utils_and_entities.BeamUtils
import kotlin.text.toFloat
import kotlin.times

object DarkElement : IElementalSchool {
    override fun getIronSchool() = SchoolRegistry.BLOOD.get() // Или Evocation

    override fun getManaCost(power: Double, index: Int) = (power * 25f).toFloat()
    override fun getMediaCost(power: Double, index: Int) = (power * 5000L).toLong()

    override fun createProjectile(index: Int, world: Level, caster: LivingEntity, damage: Double): Projectile? {
        val dmg = damage.toFloat()*1.5.toFloat()
        return when (index) {
    1 -> setup(EntityRegistry.BLOOD_NEEDLE.get().create(world), caster, dmg)
    2 -> setup(EntityRegistry.WITHER_SKULL_PROJECTILE.get().create(world), caster, dmg)
    3 -> setup(EntityRegistry.BLOOD_SLASH_PROJECTILE.get().create(world), caster, dmg)
    4 -> setup(EntityRegistry.CREEPER_HEAD_PROJECTILE.get().create(world), caster, dmg)
    else -> null}
    }
    override fun createContinuousEffect(index: Int, world: Level, caster: LivingEntity, direction: Vec3, power: Double, duration: Int) {}
    override fun createPointEffect(index: Int, world: Level, caster: LivingEntity, pos: Vec3, damage: Double) {
        if (world.isClientSide || world !is ServerLevel) return
        val dmg = damage.toFloat()

        when (index) {
            1 -> { // Щупальца Бездны (Sculk Tentacles)
                val tentacles = io.redspace.ironsspellbooks.registries.EntityRegistry.SCULK_TENTACLE.get().create(world)
                if (tentacles != null) {
                    tentacles.owner = caster
                    tentacles.setPos(pos.x, pos.y, pos.z)
                    world.addFreshEntity(tentacles)
                }
            }
            2 -> { // Сингулярность Энда (Black Hole)
                val blackHole = io.redspace.ironsspellbooks.registries.EntityRegistry.BLACK_HOLE.get().create(world)
                if (blackHole != null) {
                    blackHole.owner = caster
                    blackHole.setPos(pos.x, pos.y, pos.z)
                    // Настраиваем радиус засасывания в зависимости от силы урона
                    blackHole.radius = (2.0 + (damage * 0.3)).toFloat()
                    world.addFreshEntity(blackHole)
                }
            }
        }
    }

    override fun createInstantEffect(index: Int, world: Level, caster: LivingEntity, dir: Vec3, power: Double) {
        if (world.isClientSide || world !is ServerLevel) return
        val startPos = caster.eyePosition
        val normalizedDir = dir.normalize()

        when (index) {
            1 -> { // ELDRITCH BLAST
                val maxRange = 25.0
                val hitResult = caster.pick(maxRange, 1.0f, false)
                val endPos = hitResult.location

                // Спавним готовую сущность визуала Бездны Ирона
                val voidRay = io.redspace.ironsspellbooks.registries.EntityRegistry.ELDRITCH_BLAST_VISUAL_ENTITY.get().create(world)
                if (voidRay != null) {
                    voidRay.setPos(caster.x, caster.eyeY - 0.3, caster.z)
                    voidRay.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES, endPos)
                    world.addFreshEntity(voidRay)
                }

                if (hitResult is net.minecraft.world.phys.EntityHitResult && hitResult.entity is LivingEntity) {
                    val target = hitResult.entity as LivingEntity
                    if (!target.isAlliedTo(caster)) {
                        target.hurt(world.damageSources().magic(), (8.0 * power).toFloat())
                    }
                }
                world.playSound(null, caster.blockPosition(), SoundEvents.EVOKER_CAST_SPELL, SoundSource.PLAYERS, 1.0f, 1.2f)
            }
        }
    }
    override fun getBehavior(index: Int) = when(index) {
        1 -> BeamUtils.BeamType.CONE // Breath
        2 -> BeamUtils.BeamType.BEAM // Ray
        else -> BeamUtils.BeamType.BEAM
    }
    override fun performBeamHit(world: Level, caster: LivingEntity, target: Entity, power: Double) {}

        private fun setup(p: Entity?, caster: LivingEntity, dmg: Float): Projectile? {
        if (p is AbstractMagicProjectile) {
            p.owner = caster
            p.damage = dmg
        }
        return p as? Projectile
    }
    private fun drawInstantLineParticles(world: ServerLevel, start: Vec3, end: Vec3, particle: net.minecraft.core.particles.ParticleOptions, count: Int) {
        for (i in 0..count) {
            val ratio = i / count.toDouble()
            val pos = start.lerp(end, ratio)
            world.sendParticles(particle, pos.x, pos.y, pos.z, 1, 0.0, 0.0, 0.0, 0.0)
        }
    }
    private fun spawnEntity(type: EntityType<*>, world: Level, pos: Vec3) {
        type.create(world)?.let {
            it.moveTo(pos.x, pos.y, pos.z)
            world.addFreshEntity(it)
        }
    }
}