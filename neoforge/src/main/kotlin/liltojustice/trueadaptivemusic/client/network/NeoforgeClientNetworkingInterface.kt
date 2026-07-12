package liltojustice.trueadaptivemusic.client.network

import io.netty.buffer.ByteBuf
import liltojustice.trueadaptivemusic.network.ClientNetworkInterface
import liltojustice.trueadaptivemusic.network.model.Context
import liltojustice.trueadaptivemusic.network.transformHandler
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.neoforged.neoforge.client.network.ClientPacketDistributor
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent

object NeoforgeClientNetworkingInterface: ClientNetworkInterface {
    private const val REGISTRAR_VERSION = "1"
    private var payloadHandlersEvent: RegisterPayloadHandlersEvent? = null

    fun registerPayloadHandlers(event: RegisterPayloadHandlersEvent) {
        payloadHandlersEvent = event
    }

    override fun <T: CustomPacketPayload> registerClientboundPacket(
        type: CustomPacketPayload.Type<T>,
        codec: StreamCodec<ByteBuf, T>,
        handler: (payload: T, context: Context) -> Unit
    ) {
        val registrar = payloadHandlersEvent?.registrar(REGISTRAR_VERSION) ?: return
        registrar.playToClient(type, codec, transformHandler(handler))
    }

    override fun sendToServer(payload: CustomPacketPayload) {
        ClientPacketDistributor.sendToServer(payload)
    }
}