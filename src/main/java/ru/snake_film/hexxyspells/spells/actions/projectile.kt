package ru.snake_film.hexxyspells.spells.actions

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.getVec3
import at.petrak.hexcasting.api.casting.getPositiveDouble
import at.petrak.hexcasting.api.casting.getInt
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.phys.Vec3
import ru.snake_film.hexxyspells.HexxyUtils
import ru.snake_film.hexxyspells.spells.ElementalRegistry
import ru.snake_film.hexxyspells.spells.ManaMediaCost
import ru.snake_film.hexxyspells.spells.actions.pointAction

object projectile : SpellAction {
    override val argc = 3

    override fun execute(args: List<Iota>, env: CastingEnvironment): SpellAction.Result {
        val player = env.caster as? ServerPlayer ?: return SpellAction.Result(pointAction.EmptySpell, 0, listOf())

        val elementId = player.persistentData.getString("hexxyspells:current_element").ifEmpty { "spirit" }
        val element = ElementalRegistry.get(elementId)

        val spellIndex = args.getInt(0, argc)
        val power = args.getPositiveDouble(1, argc)
        val direction = args.getVec3(2, argc)

        if (!ManaMediaCost.checkAndConsume(player, ManaMediaCost.TYPE_PROJECTILE, power.toDouble(), spellIndex)) {
            return SpellAction.Result(pointAction.EmptySpell, 0, listOf())
        }
        val mediaCost = element.getMediaCost(power, spellIndex)

        return SpellAction.Result(
            ProjectileSpell(elementId, spellIndex, power, direction),
            mediaCost,
            listOf(ParticleSpray.burst(player.position(), 1.0))
        )
    }

    private data class ProjectileSpell(val elementId: String, val index: Int, val power: Double, val dir: Vec3) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            val caster = env.caster ?: return
            val element = ElementalRegistry.get(elementId)

            val projectileEntity = element.createProjectile(index, env.world, caster, power) ?: return

            projectileEntity.setPos(caster.eyePosition.x, caster.eyePosition.y, caster.eyePosition.z)
            val speed = HexxyUtils.calculateSpeed(power)

            if (projectileEntity is net.minecraft.world.entity.projectile.Projectile) {
                projectileEntity.shoot(dir.x, dir.y, dir.z, dir.length().toFloat() * speed, 0f)
            }
            env.world.addFreshEntity(projectileEntity)
        }
    }
}