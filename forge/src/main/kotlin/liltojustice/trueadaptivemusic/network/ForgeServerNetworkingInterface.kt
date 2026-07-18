package liltojustice.trueadaptivemusic.network

import liltojustice.trueadaptivemusic.network.model.Context
import liltojustice.trueadaptivemusic.network.model.CustomPacketPayload
import liltojustice.trueadaptivemusic.network.model.CustomPacketPayloadType
import liltojustice.trueadaptivemusic.network.model.ForgeDynamicPacketPayload
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraftforge.network.NetworkDirection
import net.minecraftforge.network.NetworkRegistry
import net.minecraftforge.network.PacketDistributor

object ForgeServerNetworkingInterface: ServerNetworkInterface {
    private const val REGISTRAR_VERSION = "1"
    private var channel = NetworkRegistry.newSimpleChannel(
        ResourceLocation("trueadaptivemusic", "main"),
        { REGISTRAR_VERSION },
        REGISTRAR_VERSION::equals,
        REGISTRAR_VERSION::equals
    )
    private var messageId = 0

    override fun <T : CustomPacketPayload> registerServerboundPacket(
        type: CustomPacketPayloadType<T>,
        handler: (payload: T, context: Context) -> Unit
    ) {
        channel.registerMessage(
            messageId++,
            ForgeDynamicPacketPayload::class.java,
            { msg, buf -> type.write(msg.payload as T, buf) },
            { buf -> ForgeDynamicPacketPayload(type.read(buf)) },
            { msg, contextGetter ->
                contextGetter.get()
                    .takeIf { it.direction == NetworkDirection.PLAY_TO_SERVER }
                    ?.let { it.sender?.let { player -> handler(msg.payload as T, Context(player)) } }
            }
        )
    }

    override fun <T : CustomPacketPayload> sendToClient(
        player: ServerPlayer,
        type: CustomPacketPayloadType<T>,
        payload: T
    ) {
        channel.send(PacketDistributor.PLAYER.with { player }, ForgeDynamicPacketPayload(payload))
    }
}