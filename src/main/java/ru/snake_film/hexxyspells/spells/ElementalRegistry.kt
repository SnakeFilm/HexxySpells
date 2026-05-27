package ru.snake_film.hexxyspells.spells

import io.redspace.ironsspellbooks.api.magic.MagicData
import net.minecraft.server.level.ServerPlayer
import ru.snake_film.hexxyspells.spells.AirElement
import ru.snake_film.hexxyspells.spells.DarkElement
import ru.snake_film.hexxyspells.spells.EarthElement
import ru.snake_film.hexxyspells.spells.FireElement
import ru.snake_film.hexxyspells.spells.IElementalSchool
import ru.snake_film.hexxyspells.spells.SpiritElement
import ru.snake_film.hexxyspells.spells.WaterElement

object ElementalRegistry {
    private val schools = mapOf(
        "fire" to FireElement,
        "water" to WaterElement,
        "spirit" to SpiritElement,
        "dark" to DarkElement,
        "earth" to EarthElement,
        "air" to AirElement
    )

    fun get(id: String): IElementalSchool = schools[id.lowercase()] ?: SpiritElement

    // Динамическая трата маны в зависимости от выбранной стихии
    fun tryConsumeMana(player: ServerPlayer, elementId: String, power: Double, index: Int): Boolean {
        val element = get(elementId)
        val amount = element.getManaCost(power, index)

        val magicData = MagicData.getPlayerMagicData(player)
        if (magicData.mana < amount) return false

        magicData.mana -= amount
        return true
    }
}