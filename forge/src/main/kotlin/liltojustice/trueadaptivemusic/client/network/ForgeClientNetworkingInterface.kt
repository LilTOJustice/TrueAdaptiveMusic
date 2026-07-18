package liltojustice.trueadaptivemusic.client.network

import liltojustice.trueadaptivemusic.network.ClientNetworkInterface
import liltojustice.trueadaptivemusic.network.model.Context
import liltojustice.trueadaptivemusic.network.model.CustomPacketPayload
import liltojustice.trueadaptivemusic.network.model.CustomPacketPayloadType

object ForgeClientNetworkingInterface: ClientNetworkInterface {
    override fun <T : CustomPacketPayload> registerClientboundPacket(
        type: CustomPacketPayloadType<T>,
        handler: (payload: T, context: Context) -> Unit
    ) {
    }

    override fun <T : CustomPacketPayload> sendToServer(
        type: CustomPacketPayloadType<T>,
        payload: T
    ) {
    }
}