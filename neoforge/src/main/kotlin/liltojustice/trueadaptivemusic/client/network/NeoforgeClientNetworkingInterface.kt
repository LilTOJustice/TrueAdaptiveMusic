package liltojustice.trueadaptivemusic.client.network

import io.netty.buffer.ByteBuf
import liltojustice.trueadaptivemusic.network.ClientNetworkInterface
import liltojustice.trueadaptivemusic.network.model.Context
import liltojustice.trueadaptivemusic.network.transformHandler
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.neoforged.neoforge.client.network.ClientPacketDistributor
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent
import net.neoforged.neoforge.network.registration.PayloadRegistrar

object NeoforgeClientNetworkingInterface: ClientNetworkInterface {
    private const val REGISTRAR_VERSION = "1"
    private val registrations = mutableListOf<(RegisterClientPayloadHandlersEvent) -> Unit>()
    private val commonRegistrations = mutableListOf<(PayloadRegistrar) -> Unit>()

    fun registerCommonPayloadHandlers(event: RegisterPayloadHandlersEvent) {
        val registrar = event.registrar(REGISTRAR_VERSION)
        commonRegistrations.forEach { it(registrar) }
    }

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
        commonRegistrations.add { registrar ->
            registrar.playToClient(type, codec)
        }
    }

    override fun sendToServer(payload: CustomPacketPayload) {
        ClientPacketDistributor.sendToServer(payload)
    }
}