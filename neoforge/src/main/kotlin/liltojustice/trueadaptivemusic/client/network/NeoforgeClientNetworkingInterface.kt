package liltojustice.trueadaptivemusic.client.network

import io.netty.buffer.ByteBuf
import liltojustice.trueadaptivemusic.network.ClientNetworkInterface
import liltojustice.trueadaptivemusic.network.model.Context
import liltojustice.trueadaptivemusic.network.transformHandler
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.neoforged.neoforge.network.PacketDistributor
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent
import net.neoforged.neoforge.network.registration.PayloadRegistrar

object NeoforgeClientNetworkingInterface: ClientNetworkInterface {
    private const val REGISTRAR_VERSION = "1"
    private val registrations = mutableListOf<(PayloadRegistrar) -> Unit>()

    fun registerPayloadHandlers(event: RegisterPayloadHandlersEvent) {
        val registrar = event.registrar(REGISTRAR_VERSION)
        registrations.forEach { it(registrar) }
    }

    override fun <T: CustomPacketPayload> registerClientboundPacket(
        type: CustomPacketPayload.Type<T>,
        codec: StreamCodec<ByteBuf, T>,
        handler: (payload: T, context: Context) -> Unit
    ) {
        registrations.add { event ->
            event.playToClient(type, codec, transformHandler(handler))
        }
    }

    override fun <T : CustomPacketPayload> registerServerboundPacket(
        type: CustomPacketPayload.Type<T>,
        codec: StreamCodec<ByteBuf, T>
    ) {
    }

    override fun sendToServer(payload: CustomPacketPayload) {
        PacketDistributor.sendToServer(payload)
    }
}