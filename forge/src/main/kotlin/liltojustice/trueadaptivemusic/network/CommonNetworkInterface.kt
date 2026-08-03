package liltojustice.trueadaptivemusic.network

import liltojustice.trueadaptivemusic.network.model.Context
import liltojustice.trueadaptivemusic.network.model.CustomPacketPayload
import liltojustice.trueadaptivemusic.network.model.CustomPacketPayloadType
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Player
import net.minecraftforge.network.NetworkDirection
import net.minecraftforge.network.NetworkRegistry
import net.minecraftforge.network.simple.SimpleChannel

object CommonNetworkInterface {
    val channel: SimpleChannel = NetworkRegistry.newSimpleChannel(
        ResourceLocation("trueadaptivemusic", "main"),
        { REGISTRAR_VERSION },
        REGISTRAR_VERSION::equals,
        REGISTRAR_VERSION::equals
    )

    private const val REGISTRAR_VERSION = "1"
    private var messageId = 0

    @Suppress("warnings")
    fun <T : CustomPacketPayload> registerToServer(
        type: CustomPacketPayloadType<T>,
        handler: (payload: T, context: Context) -> Unit
    ) {
        channel.registerMessage(
            messageId++,
            type.payloadClass.java,
            { msg, buf -> type.write(msg, buf) },
            { buf -> type.read(buf) },
            { msg, contextGetter ->
                contextGetter.get()
                    .takeIf { it.direction == NetworkDirection.PLAY_TO_SERVER }
                    ?.let {
                        try {
                            it.sender?.let { player -> handler(msg, Context(player)) }
                            it.packetHandled = true
                        }
                        catch (_: Exception) {}
                    }
            }
        )
    }

    @Suppress("warnings")
    fun <T: CustomPacketPayload> registerToClient(
        playerGetter: () -> Player?,
        type: CustomPacketPayloadType<T>,
        handler: (payload: T, context: Context) -> Unit
    ) {
        channel.registerMessage(
            messageId++,
            type.payloadClass.java,
            { msg, buf -> type.write(msg, buf) },
            { buf -> type.read(buf) },
            { msg, contextGetter ->
                contextGetter.get()
                    .takeIf { it.direction == NetworkDirection.PLAY_TO_CLIENT }
                    ?.let {
                        try {
                            playerGetter()?.let { player -> handler(msg, Context(player)) }
                            it.packetHandled = true
                        }
                        catch (_: Exception) {}
                    }
            }
        )
    }

    @Suppress("warnings")
    fun <T: CustomPacketPayload> registerToClient(type: CustomPacketPayloadType<T>) {
        channel.registerMessage(
            messageId++,
            type.payloadClass.java,
            { msg, buf -> type.write(msg, buf) },
            { buf -> type.read(buf) },
            { msg, contextGetter ->
                contextGetter.get()
                    .takeIf { it.direction == NetworkDirection.PLAY_TO_CLIENT }
                    ?.let {
                        try {
                            it.packetHandled = true
                        }
                        catch (_: Exception) {}
                    }
            }
        )
    }
}