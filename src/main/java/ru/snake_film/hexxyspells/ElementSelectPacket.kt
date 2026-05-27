package ru.snake_film.hexxyspells


import at.petrak.hexcasting.api.casting.iota.PatternIota
import at.petrak.hexcasting.api.casting.math.HexDir
import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes
import net.minecraft.network.FriendlyByteBuf
import net.minecraftforge.network.NetworkEvent
import java.util.function.Supplier
import at.petrak.hexcasting.api.casting.eval.vm.CastingVM

import at.petrak.hexcasting.api.casting.eval.sideeffects.EvalSound
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.world.InteractionHand
import ru.snake_film.hexxyspells.elements.ElementalCastingManager

class ElementSelectPacket(val element: String) {
    fun encode(buf: FriendlyByteBuf) {
        buf.writeUtf(element)
    }

    companion object {
        fun decode(buf: FriendlyByteBuf): ElementSelectPacket = ElementSelectPacket(buf.readUtf())

        // Мапа паттернов для каждой стихии (углы и направление)

        val ELEMENT_PATTERNS = mapOf(
            "spirit" to HexPattern.fromAngles("waqqqqqwaeaeaeaeaea", HexDir.SOUTH_EAST),
            "fire" to HexPattern.fromAngles("wqqqqa", HexDir.SOUTH_EAST),
            "water" to HexPattern.fromAngles("wqqqqw", HexDir.NORTH_WEST),
            "earth" to HexPattern.fromAngles("aqawwawwaw", HexDir.SOUTH_WEST),
            "air" to HexPattern.fromAngles("aqqqqwqq", HexDir.NORTH_EAST),
            "dark" to HexPattern.fromAngles("qaqqqqqwqqqeaeaeaeadaeaeaea", HexDir.EAST)
        )
    }

    fun handle(ctx: Supplier<NetworkEvent.Context>) {
        val context = ctx.get()
        context.enqueueWork {
            val player = context.sender ?: return@enqueueWork
            val vm = IXplatAbstractions.INSTANCE.getStaffcastVM(player, InteractionHand.MAIN_HAND) ?: return@enqueueWork


            val elementPattern = ELEMENT_PATTERNS[element]
            if (elementPattern != null) {

                player.persistentData.putString("hexxyspells:current_element", element)
            }
        }
    }
}