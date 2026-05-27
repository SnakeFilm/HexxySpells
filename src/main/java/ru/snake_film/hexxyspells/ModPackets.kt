    package ru.snake_film.hexxyspells

    import net.minecraft.resources.ResourceLocation
    import net.minecraftforge.network.NetworkRegistry
    import net.minecraftforge.network.simple.SimpleChannel
    import ru.snake_film.hexxyspells.MouseUpdatePacket // Проверь этот импорт!

    object ModPackets {
        private const val PROTOCOL_VERSION = "1"
        @JvmField
        val CHANNEL: SimpleChannel = NetworkRegistry.newSimpleChannel(
            ResourceLocation("hexxyspells", "main"),
            { PROTOCOL_VERSION },
            { it == PROTOCOL_VERSION },
            { it == PROTOCOL_VERSION }
        )


        fun register() {
            var id = 0
            CHANNEL.registerMessage(
                id++,
                MouseUpdatePacket::class.java,
                { msg, buf -> msg.encode(buf) },
                { buf -> MouseUpdatePacket.decode(buf) },
                { msg, ctx -> msg.handle(ctx) }
            )
            CHANNEL.registerMessage(
                id++,
                ElementSelectPacket::class.java,
                { msg, buf -> msg.encode(buf) },
                { buf -> ElementSelectPacket.decode(buf) },
                { msg, ctx -> msg.handle(ctx) }
            )
        }
    }