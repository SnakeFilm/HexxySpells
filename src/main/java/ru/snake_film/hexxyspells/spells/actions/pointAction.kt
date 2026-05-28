package ru.snake_film.hexxyspells.spells.actions

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.getVec3
import at.petrak.hexcasting.api.casting.getPositiveDouble
import at.petrak.hexcasting.api.casting.getInt
import io.redspace.ironsspellbooks.api.magic.MagicData
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec3
import ru.snake_film.hexxyspells.HexxyUtils
import ru.snake_film.hexxyspells.iota.StringIota
import ru.snake_film.hexxyspells.oldspells.school_id
import ru.snake_film.hexxyspells.spells.ElementalRegistry
import ru.snake_film.hexxyspells.spells.ManaMediaCost
import kotlin.math.sqrt

object pointAction : SpellAction {
    override val argc = 3

    override fun execute(args: List<Iota>, env: CastingEnvironment): SpellAction.Result {
        val player = env.caster as? ServerPlayer ?: return SpellAction.Result(EmptySpell, 0, listOf())

        val elementId = player.persistentData.getString("hexxyspells:current_element").ifEmpty { "spirit" }
        val element = ElementalRegistry.get(elementId)

        val spellIndex = args.getInt(0, argc)
        val rawPower = args.getPositiveDouble(1, argc)
        val pointPos = args.getVec3(2, argc)


        val distance = player.eyePosition.distanceTo(pointPos)
        val power = rawPower.toFloat()*0.3f


        if (!ManaMediaCost.checkAndConsume(player, ManaMediaCost.TYPE_POINT, power.toDouble(), spellIndex, distance)) {
            return SpellAction.Result(EmptySpell, 0, listOf())
        }
        val mediaCost = element.getMediaCost(rawPower, spellIndex)

        return SpellAction.Result(
            PointSpell(elementId, spellIndex, power.toDouble(), pointPos),
            mediaCost,
            listOf(ParticleSpray.burst(pointPos, 1.0))
        )
    }

    public object EmptySpell : RenderedSpell {
        override fun cast(env: CastingEnvironment) {}
    }

    private data class PointSpell(val elementId: String, val index: Int, val power: Double, val pos: Vec3) :
        RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            val caster = env.caster ?: return
            val element = ElementalRegistry.get(elementId)

            element.createPointEffect(index, env.world, caster, pos, power)
        }
    }
}