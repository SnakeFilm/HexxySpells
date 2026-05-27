package ru.snake_film.hexxyspells.spells.actions

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.getVec3
import at.petrak.hexcasting.api.casting.getPositiveDouble
import at.petrak.hexcasting.api.casting.getInt
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.phys.Vec3
import ru.snake_film.hexxyspells.spells.ElementalRegistry
import ru.snake_film.hexxyspells.spells.ManaMediaCost

object InstantAction : SpellAction {
    override val argc = 3 // индекс, сила, вектор

    override fun execute(args: List<Iota>, env: CastingEnvironment): SpellAction.Result {
        val player = env.caster as? ServerPlayer ?: return SpellAction.Result(pointAction.EmptySpell, 0, listOf())

        val elementId = player.persistentData.getString("hexxyspells:current_element")
        val element = ElementalRegistry.get(elementId)

        val spellIndex = args.getInt(0, argc)
        val power = args.getPositiveDouble(1, argc)
        val direction = args.getVec3(2, argc)

        if (!ManaMediaCost.checkAndConsume(player, ManaMediaCost.TYPE_INSTANT, power, spellIndex)) {
            return SpellAction.Result(pointAction.EmptySpell, 0, listOf())
        }

        val mediaCost = element.getMediaCost(power, spellIndex)

        return SpellAction.Result(
            InstantSpell(elementId, spellIndex, power, direction),
            mediaCost,
            listOf(ParticleSpray.burst(player.position(), 1.0))
        )

    }
    fun drawLaserBeam(world: ServerLevel, start: Vec3, end: Vec3, particle: net.minecraft.core.particles.ParticleOptions, count: Int = 12) {
        val distance = start.distanceTo(end)
        val dir = end.subtract(start).normalize()
        val step = distance / count
        for (i in 0..count) {
            val pos = start.add(dir.scale(i * step))
            world.sendParticles(particle, pos.x, pos.y, pos.z, 1, 0.0, 0.0, 0.0, 0.0)
        }
    }

    private data class InstantSpell(val elementId: String, val index: Int, val power: Double, val dir: Vec3) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            val caster = env.caster as? LivingEntity ?: return
            val world = caster.level()

            if (!world.isClientSide && world is ServerLevel) {
                val school = ElementalRegistry.get(elementId)

                school.createInstantEffect(index, world, caster, dir, power)
            }
        }
    }
}