package liltojustice.trueadaptivemusic.client.network

import io.netty.buffer.ByteBuf
import liltojustice.trueadaptivemusic.network.ClientNetworkInterface
import liltojustice.trueadaptivemusic.network.model.Context
import liltojustice.trueadaptivemusic.network.transformHandler
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.neoforged.neoforge.client.network.ClientPacketDistributor
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent

object NeoforgeClientNetworkingInterface: ClientNetworkInterface {
    private val registrations = mutableListOf<(RegisterClientPayloadHandlersEvent) -> Unit>()

    fun registerPayloadHandlers(event: RegisterClientPayloadHandlersEvent) {
        registrations.forEach { it(event) }
    }

    override fun <T: CustomPacketPayload> registerClientboundPacket(
        type: CustomPacketPayload.Type<T>,
        codec: StreamCodec<ByteBuf, T>,
        handler: (payload: T, context: Context) -> Unit
    ) {
        registrations.add { event ->
            event.register(type, transformHandler(handler))
        }
    }

    override fun sendToServer(payload: CustomPacketPayload) {
        PacketDistributor.sendToServer(payload)
    }
}