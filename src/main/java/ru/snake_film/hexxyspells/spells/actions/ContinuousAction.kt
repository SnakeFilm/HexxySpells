package ru.snake_film.hexxyspells.spells.actions

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.getPositiveDouble
import at.petrak.hexcasting.api.casting.getInt
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.phys.Vec3
import ru.snake_film.hexxyspells.spells.ElementalRegistry
import ru.snake_film.hexxyspells.spells.ManaMediaCost


object ContinuousAction : SpellAction {

    override val argc = 3

    override fun execute(args: List<Iota>, env: CastingEnvironment): SpellAction.Result {
        val player = env.caster as? ServerPlayer ?: return SpellAction.Result(pointAction.EmptySpell, 0, listOf())

        val elementId = player.persistentData.getString("hexxyspells:current_element").ifEmpty { "spirit" }
        val element = ElementalRegistry.get(elementId)
        if (elementId.isEmpty()) return SpellAction.Result(pointAction.EmptySpell, 0, listOf())

        val spellIndex = args.getInt(0, argc)
        val power = args.getPositiveDouble(1, argc)
        val duration = args.getInt(2, argc) // Длительность действия в тиках
        val direction = player.lookAngle

        if (!ManaMediaCost.checkAndConsume(player, ManaMediaCost.TYPE_CONTINUOUS, power, spellIndex)) {
            return SpellAction.Result(pointAction.EmptySpell, 0, listOf())
        }

        val mediaCost = element.getMediaCost(power, spellIndex)

        return SpellAction.Result(
            ContinuousSpell(elementId, spellIndex, power, duration, direction),
            mediaCost,
            listOf(ParticleSpray.burst(player.position(), 1.0))
        )
    }

    private data class ContinuousSpell(
        val elementId: String,
        val index: Int,
        val power: Double,
        val duration: Int,
        val direction: Vec3
    ) : RenderedSpell {

        override fun cast(env: CastingEnvironment) {
            val caster = env.caster as? LivingEntity ?: return
            val world = caster.level()

            val school = ElementalRegistry.get(elementId)

            school.createContinuousEffect(index, world, caster, direction, power, duration)
        }
    }
}