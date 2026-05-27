    package ru.snake_film.hexxyspells.elements

    import at.petrak.hexcasting.api.casting.iota.IotaType
    import at.petrak.hexcasting.api.casting.iota.PatternIota
    import at.petrak.hexcasting.api.casting.math.HexPattern
    import net.minecraft.nbt.CompoundTag
    import net.minecraft.nbt.Tag
    import net.minecraft.network.chat.Component
    import net.minecraft.server.level.ServerLevel
    import net.minecraft.resources.ResourceLocation
    import ru.snake_film.hexxyspells.ElementSelectPacket
    import ru.snake_film.hexxyspells.HexxySpells

    class ElementalPatternIota(pattern: HexPattern, val element: String) : PatternIota(pattern) {

        override fun getType(): IotaType<*> = TYPE

        override fun serialize(): Tag {
            val tag = super.serialize() as CompoundTag
            tag.putString("element", element)
            return tag
        }

        // Вспомогательный метод для получения цвета по названию стихии
        fun getElementColor(): Int {
            return when (element.lowercase()) {
                "fire" -> 0xFF_FF4500.toInt()   // Оранжево-красный
                "water" -> 0xFF_00BFFF.toInt()  // Голубой
                "earth" -> 0xFF_8B4513.toInt()  // Коричневый
                "air" -> 0xFF_F0FFFF.toInt()    // Белый/Воздушный
                "dark" -> 0xFF_4B0082.toInt()   // Темно-фиолетовый
                else -> 0xFF_7386FF.toInt()     // Стандартный синий Hex
            }
        }


        companion object {
            val TYPE: IotaType<ElementalPatternIota> = object : IotaType<ElementalPatternIota>() {
                override fun color(): Int = 0xFF_B18FEF.toInt()

                override fun deserialize(tag: Tag, world: ServerLevel): ElementalPatternIota? {
                    val ct = tag as? CompoundTag ?: return null
                    val pattern = HexPattern.fromNBT(ct)
                    val element = if (ct.contains("element")) ct.getString("element") else "spirit"
                    return ElementalPatternIota(pattern, element)
                }

                override fun display(tag: Tag): Component {
                    val ct = tag as? CompoundTag ?: return Component.empty()
                    val pattern = HexPattern.fromNBT(ct)

                    // Формируем строку в правильном формате Hex Casting
                    // pattern.startDir.name - это направление (например, "EAST")
                    // pattern.anglesSignature() - это ваша строка (например, "deaqq")
                    val hexSignature = "<${pattern.startDir.name}, ${pattern.anglesSignature()}>"

                    val element = ct.getString("element")
                    val color = getElementColor(element)

                    // Применяем шрифт "hex" для отрисовки графической руны из текста
                    return Component.literal(hexSignature)
                        .withStyle { style ->
                            style.withFont(ResourceLocation("hexcasting", "hex"))
                                .withColor(color)
                        }
                }

                // Вынесли метод сюда, чтобы он был доступен в статическом контексте display
                private fun getElementColor(element: String): Int {
                    return when (element.lowercase()) {
                        "fire" -> 0xFFFF4500.toInt()
                        "water" -> 0xFF00BFFF.toInt()
                        "earth" -> 0xFF8B4513.toInt()
                        "air" -> 0xFFF0FFFF.toInt()
                        "dark" -> 0xFF4B0082.toInt()
                        else -> 0xFF7386FF.toInt()
                    }
                }
            }
        }
    }