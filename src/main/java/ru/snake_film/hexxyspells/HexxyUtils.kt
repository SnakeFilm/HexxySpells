package ru.snake_film.hexxyspells

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import net.minecraft.world.entity.LivingEntity
import kotlin.math.*

object HexxyUtils {

    /**
     * Рассчитывает финальный урон. Возвращает Float для совместимости с Entity.
     */
    fun calculateBalancedDamage(rawPower: Double, multiplier: Double = 2.5, cap: Double = 50.0): Float {
        val calculated = sqrt(rawPower) * multiplier
        return min(calculated, cap).toFloat()
    }

    /**
     * Рассчитывает множитель скорости. Возвращает Float.
     */
    fun calculateSpeed(rawPower: Double): Float {
        val safePower = kotlin.math.max(rawPower, 1.0)
        val result = 3.0 / kotlin.math.sqrt(safePower)

        return result.toFloat()
    }


    fun applySmartMediaCooldown(env: CastingEnvironment, media: Long) {
        val caster = env.caster ?: return
        if (caster is net.minecraft.world.entity.player.Player) {

            // 1. Рассчитываем время кулдауна
            // За каждые 500 единиц медии — 1 тик. Минимум 10 тиков.
            val ticks = (media / 1).toInt().coerceAtLeast(10)

            // 2. Список ключевых слов для блокировки (с добавлением побрякушки и штуковины)
            val itemsToBlock = listOf(
                "trinket", "artifact", "talisman", "thought_knot",
                "focus", "spellbook", "bauble", "shackme", "pawn"
            )

            // Функция проверки: содержит ли имя предмета запрещенное слово
            fun shouldBlock(stack: net.minecraft.world.item.ItemStack): Boolean {
                if (stack.isEmpty) return false
                val name = stack.item.descriptionId.lowercase()
                return itemsToBlock.any { name.contains(it) }
            }

            // 3. Проверяем обе руки
            val mainHand = caster.mainHandItem
            val offHand = caster.offhandItem

            if (shouldBlock(mainHand)) {
                caster.cooldowns.addCooldown(mainHand.item, ticks)
            }

            if (shouldBlock(offHand)) {
                caster.cooldowns.addCooldown(offHand.item, ticks)
            }
        }
    }

    }


