package ru.snake_film.hexxyspells.elements

import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn

@OnlyIn(Dist.CLIENT)
object ElementalCastingManager {
    // По умолчанию - "spirit" (стандартный Hex Casting)
    private var currentElement: String = "spirit"

    fun setElement(element: String) {
        currentElement = element
    }

    fun getElement(): String = currentElement

    // Утилитный метод для получения цвета текущей стихии
    fun getCurrentColor(): Int {
        return when (currentElement) {
            "fire" -> 0xFF_FF4500.toInt()
            "water" -> 0xFF_00BFFF.toInt()
            "earth" -> 0xFF_8B4513.toInt()
            "air" -> 0xFF_F0FFFF.toInt()
            "dark" -> 0xFF_4B0082.toInt()
            else -> 0xFF_7386FF.toInt() // Стандартный синий
        }
    }
}