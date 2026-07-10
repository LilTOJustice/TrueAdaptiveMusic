package liltojustice.trueadaptivemusic.network

import io.netty.buffer.ByteBuf
import liltojustice.trueadaptivemusic.common.network.ServerNetworkInterface
import liltojustice.trueadaptivemusic.common.network.model.Context
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.server.level.ServerPlayer
import net.neoforged.neoforge.network.PacketDistributor
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent

object NeoforgeServerNetworkingInterface: ServerNetworkInterface {
    private const val REGISTRAR_VERSION = "1"
    private var payloadHandlersEvent: RegisterPayloadHandlersEvent? = null

    fun registerPayloadHandlers(event: RegisterPayloadHandlersEvent) {
        payloadHandlersEvent = event
    }

    override fun <T: CustomPacketPayload> registerServerboundPacket(
        type: CustomPacketPayload.Type<T>,
        codec: StreamCodec<ByteBuf, T>,
        handler: (payload: T, context: Context) -> Unit
    ) {
        val registrar = payloadHandlersEvent?.registrar(REGISTRAR_VERSION) ?: return
        registrar.playToServer(type, codec, transformHandler(handler))
    }

    override fun sendToClient(player: ServerPlayer, payload: CustomPacketPayload) {
        PacketDistributor.sendToPlayer(player, payload)
    }
}