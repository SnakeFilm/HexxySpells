package ru.snake_film.hexxyspells.spells

import io.redspace.ironsspellbooks.api.magic.MagicData
import net.minecraft.server.level.ServerPlayer
import kotlin.math.pow

object ManaMediaCost {

        // Типы заклинаний для определения формулы
        const val TYPE_POINT = "point"
    const val TYPE_PROJECTILE = "projectile"
    const val TYPE_CONTINUOUS = "continuous"
    const val TYPE_INSTANT = "instant"

        // Централизованный расчет стоимости
        fun getManaCost(type: String, power: Double, index: Int, extra: Double = 0.0): Float {
            return when (type) {
                TYPE_POINT -> (power.pow(2) + extra / 5f).toFloat() // extra здесь — дистанция
                TYPE_PROJECTILE -> (power.pow(2) * 1f).toFloat()
                TYPE_CONTINUOUS -> (power.pow(2) * 0.5f).toFloat()
                TYPE_INSTANT -> (power.pow(2) * 1f).toFloat()
                else -> 0f
            }
        }

        // Метод "всё в одном": проверяет и списывает
        fun checkAndConsume(player: ServerPlayer, type: String, power: Double, index: Int, extra: Double = 0.0): Boolean {
            val cost = getManaCost(type, power, index, extra)
            val magicData = MagicData.getPlayerMagicData(player)

            if (magicData.mana < cost) return false

            magicData.mana -= cost
            return true
        }
    }
