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
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
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

object SpiritElement : IElementalSchool {
    override fun getIronSchool() = SchoolRegistry.HOLY.get()

    override fun getManaCost(power: Double, index: Int) = (power * 5f).toFloat()
    override fun getMediaCost(power: Double, index: Int) = (power * 500L).toLong()

    override fun createProjectile(index: Int, world: Level, caster: LivingEntity, damage: Double): Projectile? {
        val dmg = damage.toFloat()*1.5.toFloat()
        return when (index) {
            1 -> setup(EntityRegistry.MAGIC_MISSILE_PROJECTILE.get().create(world), caster, dmg)
            2 -> setup(EntityRegistry.MAGIC_ARROW_PROJECTILE.get().create(world), caster, dmg)
            3 -> setup(EntityRegistry.SMALL_MAGIC_ARROW.get().create(world), caster, dmg)
            4 -> setup(EntityRegistry.GUIDING_BOLT.get().create(world), caster, dmg)
            5 -> {
                val wisp = EntityRegistry.WISP.get().create(world)
                if (wisp is io.redspace.ironsspellbooks.entity.spells.wisp.WispEntity) {
                    wisp.setOwner(caster)
                    wisp.level()
                }
                wisp as? Projectile
            }

            else -> null
        }}

    override fun createPointEffect(index: Int, world: Level, caster: LivingEntity, pos: Vec3, damage: Double) {
        if (world.isClientSide || world !is ServerLevel) return
        val dmg = damage.toFloat()

        when (index) {

            1 -> { // Круг Исцеления (Healing AOE)
                val healingArea = io.redspace.ironsspellbooks.registries.EntityRegistry.HEALING_AOE.get().create(world)
                if (healingArea != null) {
                    healingArea.owner = caster
                    // Лечение скейлится от переданного параметра
                    healingArea.damage = -dmg // Отрицательный урон в Iron's хилит
                    healingArea.setPos(pos.x, pos.y, pos.z)
                    world.addFreshEntity(healingArea)
                }
            }
            2 -> { // Залп Стрел сверху (Arrow Volley)
                val volley = io.redspace.ironsspellbooks.registries.EntityRegistry.ARROW_VOLLEY_ENTITY.get().create(world)
                if (volley != null) {
                    volley.owner = caster
                    volley.damage = dmg
                    volley.setPos(pos.x, pos.y, pos.z)
                    world.addFreshEntity(volley)
                }
            }
        }
    }
    override fun createInstantEffect(index: Int, world: Level, caster: LivingEntity, pos: Vec3, power: Double) {

        if (world.isClientSide || world !is ServerLevel) return
        val startPos = caster.eyePosition


        when (index) {

            1 -> {
                val groundPos = caster.position()
                val radius = 4.0 + (power * 0.5)
                val runesCount = 5 // 5 кастомных столбиков-рун как в игре

                for (i in 0 until runesCount) {
                    val angle = i * (2 * Math.PI / runesCount)
                    val x = groundPos.x + Math.cos(angle) * radius
                    val z = groundPos.z + Math.sin(angle) * radius

                    // Ищем точную высоту земли под руной
                    val blockHit = world.clip(
                        ClipContext(
                            Vec3(x, groundPos.y + 2.0, z),
                            Vec3(x, groundPos.y - 3.0, z),
                            ClipContext.Block.COLLIDER,
                            ClipContext.Fluid.NONE,
                            caster
                        )
                    )
                    val spawnY = if (blockHit.type == HitResult.Type.BLOCK) blockHit.location.y else groundPos.y

                    // ТВОЯ СУЩНОСТЬ ДЛЯ РУНЫ ИРДЕНА:
                    // val irdenRune = YourEntityRegistry.IRDEN_RUNE.get().create(world)
                    // irdenRune?.moveTo(x, spawnY, z, 0f, 0f)
                    // world.addFreshEntity(irdenRune)

                    // Пока сущность готовится, бахнем неоновые вспышки Ирона (WISP)
                    world.sendParticles(
                        io.redspace.ironsspellbooks.registries.ParticleRegistry.WISP_PARTICLE.get(),
                        x, spawnY + 0.2, z,
                        3, 0.1, 0.4, 0.1, 0.01
                    )
                }
                world.playSound(
                    null,
                    caster.blockPosition(),
                    SoundEvents.EVOKER_PREPARE_ATTACK,
                    SoundSource.PLAYERS,
                    1.0f,
                    1.4f
                )
            }
            2 -> {
                val center = caster.position().add(0.0, caster.bbHeight / 2.0, 0.0)

                // Вместо тяжелого двойного цикла по сфере, мы используем встроенную магическую вспышку
                // Ирон рендерит барьеры через кастомный пакет, но мы можем создать яркий щитовой кокон:
                world.sendParticles(
                    io.redspace.ironsspellbooks.registries.ParticleRegistry.WISP_PARTICLE.get(),
                    center.x, center.y, center.z,
                    30, 0.6, 0.8, 0.6, 0.02 // Заполняем объем вокруг игрока светящимся неоном
                )

                // Накладываем на кастера родной статус-эффект Ирона "Щит" или поглощение урона
                caster.addEffect(MobEffectInstance(MobEffects.ABSORPTION, (200 * power).toInt(), power.toInt()))
                world.playSound(null, caster.blockPosition(), io.redspace.ironsspellbooks.registries.SoundRegistry.FORCE_IMPACT.get(), SoundSource.PLAYERS, 1.0f, 1.5f)
            }
            else -> null


       }
    }
    override fun createContinuousEffect(index: Int, world: Level, caster: LivingEntity, direction: Vec3, power: Double, duration: Int) {
        val entity = ChannelingEffectEntity(world, caster, "spirit", power, duration, index, BeamUtils.BeamType.CONE)

        when (index) {
            1 -> {
                // Dragon Breath: поведение остается CONE
                entity.behavior = BeamUtils.BeamType.CONE
                entity.setParticleType(ParticleTypes.DRAGON_BREATH)
                // Если у тебя есть кастомный звук или сущность для дыхания, можно установить их здесь
            }
            2 -> {
                // Sunbeam: лазер
                entity.behavior = BeamUtils.BeamType.BEAM
                entity.setVisualEntityType(io.redspace.ironsspellbooks.registries.EntityRegistry.SUNBEAM.get())
                entity.setSoundEvent(io.redspace.ironsspellbooks.registries.SoundRegistry.SUNBEAM_IMPACT.get())
            }
            3 -> {
                // Starfall: поддерживаемый каст по площади
                entity.behavior = BeamUtils.BeamType.CONE
                entity.setVisualEntityType(io.redspace.ironsspellbooks.registries.EntityRegistry.COMET.get())
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

    override fun performBeamHit(world: Level, caster: LivingEntity, target: Entity, power: Double) {}

    private fun drawInstantLineParticles(world: ServerLevel, start: Vec3, end: Vec3, particle: net.minecraft.core.particles.ParticleOptions, count: Int) {
        for (i in 0..count) {
            val ratio = i / count.toDouble()
            val pos = start.lerp(end, ratio)
            world.sendParticles(particle, pos.x, pos.y, pos.z, 1, 0.0, 0.0, 0.0, 0.0)
        }
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
