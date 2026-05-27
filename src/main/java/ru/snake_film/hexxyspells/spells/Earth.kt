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
import net.minecraft.world.level.ClipContext
import net.minecraft.world.level.Level
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3
import ru.snake_film.hexxyspells.spells.utils_and_entities.BeamUtils
import ru.snake_film.hexxyspells.spells.utils_and_entities.ChannelingEffectEntity

object EarthElement : IElementalSchool {
    override fun getIronSchool() = SchoolRegistry.NATURE.get()
    override fun getManaCost(power: Double, index: Int) = (power * 8f).toFloat()
    override fun getMediaCost(power: Double, index: Int) = (power * 5000L).toLong()

    override fun createProjectile(index: Int, world: Level, caster: LivingEntity, damage: Double): Projectile? {
        val dmg = damage.toFloat()
        return when (index) {
            1 -> setup(EntityRegistry.ACID_ORB.get().create(world), caster, dmg)
            2 -> setup(EntityRegistry.POISON_ARROW.get().create(world), caster, dmg)
            else -> null
        }}
    override fun createContinuousEffect(index: Int, world: Level, caster: LivingEntity, direction: Vec3, power: Double, duration: Int) {
        }
    override fun createPointEffect(index: Int, world: Level, caster: LivingEntity, pos: Vec3, damage: Double) {
        if (world.isClientSide || world !is ServerLevel) return
        val dmg = damage.toFloat()
                when (index) {
                    4 -> {
                        val dir = pos.subtract(caster.position())
                        val yaw = Math.toDegrees(Math.atan2(-dir.x, dir.z)).toFloat()

                        val steps = (5 + damage / 2).toInt()
                        val stomp = io.redspace.ironsspellbooks.entity.spells.StompAoe(world, steps, yaw)

                        stomp.moveTo(caster.position().x, caster.position().y, caster.position().z)
                        stomp.owner = caster
                        stomp.damage = damage.toFloat()

                        world.addFreshEntity(stomp)
                    }
                    1 -> { // Землетрясение (Earthquake)
                        val earthquake = io.redspace.ironsspellbooks.registries.EntityRegistry.EARTHQUAKE_AOE.get().create(world)
                        if (earthquake != null) {
                            earthquake.owner = caster
                            earthquake.damage = dmg
                            earthquake.setPos(pos.x, pos.y, pos.z)
                            world.addFreshEntity(earthquake)
                        }
                    }
                    2 -> { // Оплетение Корнями (Root Entity)
                        val rootZone = io.redspace.ironsspellbooks.registries.EntityRegistry.ROOT.get().create(world)
                        if (rootZone != null) {
                            rootZone.owner = caster
                            rootZone.setPos(pos.x, pos.y, pos.z)
                            world.addFreshEntity(rootZone)
                        }
                    }
                    3 -> { // Токсичное Облако (Poison Cloud)
                        val poisonCloud = io.redspace.ironsspellbooks.registries.EntityRegistry.POISON_CLOUD.get().create(world)
                        if (poisonCloud != null) {
                            poisonCloud.owner = caster
                            poisonCloud.damage = dmg
                            poisonCloud.setPos(pos.x, pos.y, pos.z)
                            world.addFreshEntity(poisonCloud)
                        }
                    }
                }
        }
    override fun createInstantEffect(index: Int, world: Level, caster: LivingEntity, dir: Vec3, power: Double) {
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