package liltojustice.trueadaptivemusic.client.network

import liltojustice.trueadaptivemusic.network.ClientNetworkInterface
import liltojustice.trueadaptivemusic.network.CommonNetworkingInterface
import liltojustice.trueadaptivemusic.network.model.Context
import liltojustice.trueadaptivemusic.network.model.CustomPacketPayload
import liltojustice.trueadaptivemusic.network.model.CustomPacketPayloadType
import net.minecraft.client.Minecraft

object ForgeClientNetworkInterface: ClientNetworkInterface {
    override fun <T : CustomPacketPayload> registerClientboundPacket(
        type: CustomPacketPayloadType<T>,
        handler: (payload: T, context: Context) -> Unit
    ) {
        CommonNetworkingInterface.registerToClient({ Minecraft.getInstance().player }, type, handler)
    }

    override fun <T : CustomPacketPayload> sendToServer(
        type: CustomPacketPayloadType<T>,
        payload: T
    ) {
        CommonNetworkingInterface.channel.sendToServer(payload)
    }
}