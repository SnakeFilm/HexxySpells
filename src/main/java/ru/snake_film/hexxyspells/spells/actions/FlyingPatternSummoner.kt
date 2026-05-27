package ru.snake_film.hexxyspells.spells.actions

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getList
import at.petrak.hexcasting.api.casting.iota.DoubleIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.PatternIota
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import ru.snake_film.hexxyspells.hud.EntityFlyingPattern
import ru.snake_film.hexxyspells.HexxySpells // Замени на свой главный класс, где лежит Registry сущности

object ActionSpawnFlyingPattern : ConstMediaAction
{


    override val argc = 1
    override val mediaCost = 1000L

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val list = args.getList(0, argc)
        val player = env.caster as? ServerPlayer ?: return listOf()

        // Вычисляем точку: 2 блока по линии взгляда
        val pos = player.getEyePosition(1.0f).add(player.lookAngle.scale(2.0))

        val world = env.world
        val entity = EntityFlyingPattern(HexxySpells.FLYING_PATTERN.get(), world)

        // Передаем данные
        entity.setPos(pos.x, pos.y, pos.z)
        entity.setOwner(player.uuid)

        entity.patterns = list.filterIsInstance<PatternIota>().toMutableList()



        val patternList = list.filterIsInstance<PatternIota>().toMutableList()
        entity.patterns = patternList


        var totalCastTime = 1.0 // Базовое время (1 сек)[cite: 37]

        patternList.forEach { iota ->

          //  totalCastTime += iota.pattern.anglesSignature().length * 1.5
        }


        val powerValue = list.filterIsInstance<DoubleIota>().firstOrNull()?.double ?: 1.0

        totalCastTime += (Math.sqrt(powerValue) * 15)

        val estimatedMediaCost = (powerValue * 5000L)
        totalCastTime += (estimatedMediaCost / 1000.0)

        entity.maxCastTime = totalCastTime.toInt().coerceAtLeast(10)

        world.addFreshEntity(entity)
        entity.syncPatterns()


        world.playSound(null, pos.x, pos.y, pos.z,
            SoundEvents.ILLUSIONER_CAST_SPELL,
            SoundSource.PLAYERS, 1.0f, 1.0f)

        return listOf()
    }
}