package liltojustice.trueadaptivemusic.network

import liltojustice.trueadaptivemusic.network.model.Context
import liltojustice.trueadaptivemusic.network.model.CustomPacketPayload
import liltojustice.trueadaptivemusic.network.model.CustomPacketPayloadType

@Suppress("UNUSED")
interface ClientNetworkInterface {
    fun <T: CustomPacketPayload> registerClientboundPacket(
        type: CustomPacketPayloadType<T>, handler: (payload: T, context: Context) -> Unit
    )

    fun <T: CustomPacketPayload> sendToServer(type: CustomPacketPayloadType<T>, payload: T)
}