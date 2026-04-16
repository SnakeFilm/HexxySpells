package ru.snake_film.hexxyspells.spells

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.getVec3
import at.petrak.hexcasting.api.casting.getPositiveDouble
import at.petrak.hexcasting.api.casting.getInt
import net.minecraft.world.phys.Vec3
import org.checkerframework.checker.units.qual.Speed
import ru.snake_film.hexxyspells.HexxyUtils
import ru.snake_film.hexxyspells.iota.StringIota

object projectile : SpellAction {

    override val argc = 4

    override fun execute(args: List<Iota>, env: CastingEnvironment): SpellAction.Result {

        val schoolIota = args[0]
        val schoolName = if (schoolIota is StringIota) {
            schoolIota.getString().lowercase()
        } else {
            ""
        }
        val spellIndex = args.getInt(1, argc)
        val rawPower = args.getPositiveDouble(2, argc)
        val direction = args.getVec3(3, argc)
        val schoolId = when (schoolName) {
            "fire"      -> 1
            "ice"       -> 2
            "blood"     -> 3
            "ender"     -> 4
            "holy"      -> 5
            "lightning" -> 6
            "nature"    -> 7
            "evocation" -> 8
            else        -> 0
        }


        val power = HexxyUtils.calculateBalancedDamage(rawPower).toFloat()
        val speed = HexxyUtils.calculateSpeed(rawPower)
        val DirVectorLength = direction.length()

        val cost = (((rawPower * 5000L) + (rawPower * DirVectorLength))).toLong()

        return SpellAction.Result(
            // Передаем speed в конструктор Spell
            Spell(schoolId, spellIndex, power, direction, rawPower, cost, speed),
            cost,
            listOf(ParticleSpray.burst(env.caster?.position() ?: Vec3.ZERO, 1.0))
        )
    }

    data class Spell(
        val schoolId: Int,
        val spellIndex: Int,
        val power: Float,
        val direction: Vec3,
        val rawPower: Double,
        val cost: Long,
        val speed: Float
    ) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            val caster = env.caster ?: return
            val world = env.world

            HexxyUtils.applySmartMediaCooldown(env, cost)

            val projectile = school_id.createProjectile(schoolId, spellIndex, world, caster, power.toDouble()) ?: return

            projectile.setPos(caster.eyePosition.x, caster.eyePosition.y, caster.eyePosition.z)
            val speedModifier = HexxyUtils.calculateSpeed(rawPower)
            val speed = direction.length().toFloat() * speedModifier

            projectile.shoot(direction.x, direction.y, direction.z, speed, 0f)
            world.addFreshEntity(projectile)
        }
    }
}