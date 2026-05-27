package ru.snake_film.hexxyspells

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.client.KeyMapping
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.LivingEntity
import org.lwjgl.glfw.GLFW
import vazkii.patchouli.client.base.PersistentData
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
        val caster = env.caster as? net.minecraft.world.entity.player.Player ?: return


        val ticks = (media / 1000).toInt().coerceAtLeast(10)


        val itemsToBlock = listOf(
            "trinket", "artifact", "talisman", "thought_knot",
            "focus", "spellbook", "bauble", "cypher", "pawn", "wand"
        )

        fun processStack(stack: net.minecraft.world.item.ItemStack) {
            if (!stack.isEmpty) {
                val name = stack.item.descriptionId.lowercase()
                // Если предмет содержит слово из списка
                if (itemsToBlock.any { name.contains(it) }) {
                    // Накладываем кулдаун именно на этот предмет
                    caster.cooldowns.addCooldown(stack.item, ticks)
                }
            }
        }

        // 3. ПРОВЕРЯЕМ ОБЕ РУКИ ВСЕГДА
        processStack(caster.mainHandItem)
        processStack(caster.offhandItem)
    }
    /*fun getEffectiveSchool(player: ServerPlayer, requestedSchool: String): String? {
        val currentElement = player.persistentData.getString("hexxyspells:current_element").ifEmpty { "spirit" }

        return if (ElementalRegistry.g(currentElement) == ElementalRegistry.get(requestedSchool)) {
            requestedSchool
        } else {
            null // Если стихия не подходит, возвращаем null
        }
    }*/
    object ElementalRegistry {
        const val SPIRIT = "spirit" // Ваниль / По умолчанию
        const val FIRE = "fire"
        const val WATER = "water"
        const val AIR = "air"
        const val EARTH = "earth"
        const val DARK = "dark"

        // Список всех стихий для циклов или проверок
        val ALL = listOf(SPIRIT, FIRE, WATER, AIR, EARTH, DARK)
    }
    }




