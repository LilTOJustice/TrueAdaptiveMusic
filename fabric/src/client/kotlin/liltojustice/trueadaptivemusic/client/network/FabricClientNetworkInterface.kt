package liltojustice.trueadaptivemusic.client.network

import liltojustice.trueadaptivemusic.network.ClientNetworkInterface
import liltojustice.trueadaptivemusic.network.model.Context
import liltojustice.trueadaptivemusic.network.model.CustomPacketPayload
import liltojustice.trueadaptivemusic.network.model.CustomPacketPayloadType
import liltojustice.trueadaptivemusic.network.model.FabricDynamicPacketPayload
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking

object FabricClientNetworkInterface: ClientNetworkInterface {
    override fun <T : CustomPacketPayload> registerClientboundPacket(
        type: CustomPacketPayloadType<T>,
        handler: (payload: T, context: Context) -> Unit
    ) {
        ClientPlayNetworking.registerGlobalReceiver(
            type.identifier, transformHandlerClient(type, handler))
    }

    override fun <T : CustomPacketPayload> sendToServer(type: CustomPacketPayloadType<T>, payload: T) {
        ClientPlayNetworking.send(FabricDynamicPacketPayload(type, payload))
    }

    private fun <T: CustomPacketPayload> transformHandlerClient(
        type: CustomPacketPayloadType<T>,
        handler: (payload: T, context: Context) -> Unit): ClientPlayNetworking.PlayChannelHandler {
        return { client, _, buf, _ ->
            client.player?.let { handler(type.read(buf), Context(it)) }
        }
    }
}