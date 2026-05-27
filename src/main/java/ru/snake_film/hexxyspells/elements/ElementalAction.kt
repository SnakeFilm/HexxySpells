package ru.snake_film.hexxyspells.elements

import at.petrak.hexcasting.api.casting.castables.Action
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.OperationResult
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.eval.sideeffects.OperatorSideEffect
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds
import net.minecraft.network.chat.Component
import net.minecraftforge.network.PacketDistributor
import ru.snake_film.hexxyspells.ElementSelectPacket
import ru.snake_film.hexxyspells.ModPackets

class ElementalAction(val element: String) : Action {
    // В новых версиях argc() больше не нужен в самом классе Action так, как раньше,
    // но если API требует реализации, то для простых экшенов это обычно 0.

    override fun operate(
        env: CastingEnvironment,
        image: CastingImage,
        continuation: SpellContinuation
    ): OperationResult {
        val player = env.caster

        if (player != null) {
            // Записываем стихию в NBT игрока
            player.persistentData.putString("hexxyspells:current_element", element)

            ModPackets.CHANNEL.send(
                PacketDistributor.PLAYER.with { player },
                ElementSelectPacket(element)
            )
        }


        // Возвращаем результат:
        // 1. Новое состояние стека (оставляем как было: image.stack)
        // 2. Новое состояние мира/данных (image)
        // 3. Продолжение (continuation)
        // 4. Звуки и эффекты
        return OperationResult(
            image,
            listOf(),
            continuation,
            HexEvalSounds.SPELL
        )
    }
}