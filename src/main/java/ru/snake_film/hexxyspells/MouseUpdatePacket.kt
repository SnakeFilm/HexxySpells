package ru.snake_film.hexxyspells

import net.minecraft.network.FriendlyByteBuf
import net.minecraftforge.network.NetworkEvent
import java.util.function.Supplier

class MouseUpdatePacket(val button: Int, val pressed: Boolean) {


    fun encode(buf: FriendlyByteBuf) {
        buf.writeInt(button)
        buf.writeBoolean(pressed)
    }

    companion object {

        fun decode(buf: FriendlyByteBuf): MouseUpdatePacket {
            return MouseUpdatePacket(buf.readInt(), buf.readBoolean())
        }
    }

    // Обработка на сервере
    fun handle(ctx: Supplier<NetworkEvent.Context>) {
        val context = ctx.get()
        context.enqueueWork {
            val player = context.sender
            if (player != null) {
                val data = player.persistentData
                if (button == 0) data.putBoolean("is_lkm_pressed", pressed)
                if (button == 1) data.putBoolean("is_pkm_pressed", pressed)
                if (button==2) player.persistentData.putBoolean("is_c_pressed", pressed)
            }
        }
        context.packetHandled = true
    }
}