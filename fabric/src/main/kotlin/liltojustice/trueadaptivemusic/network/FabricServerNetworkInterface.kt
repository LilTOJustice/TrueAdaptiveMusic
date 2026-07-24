package liltojustice.trueadaptivemusic.network

import liltojustice.trueadaptivemusic.network.model.Context
import liltojustice.trueadaptivemusic.network.model.CustomPacketPayload
import liltojustice.trueadaptivemusic.network.model.CustomPacketPayloadType
import liltojustice.trueadaptivemusic.network.model.FabricDynamicPacketPayload
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.server.level.ServerPlayer

object FabricServerNetworkInterface: ServerNetworkInterface {
    override fun <T: CustomPacketPayload> registerServerboundPacket(
        type: CustomPacketPayloadType<T>,
        handler: (payload: T, context: Context) -> Unit
    ) {
        ServerPlayNetworking.registerGlobalReceiver(
            type.identifier, transformHandlerServer(type, handler))
    }

    override fun <T: CustomPacketPayload> sendToClient(player: ServerPlayer, type: CustomPacketPayloadType<T>, payload: T) {
        ServerPlayNetworking.send(player, FabricDynamicPacketPayload(type, payload))
    }

    private fun <T: CustomPacketPayload> transformHandlerServer(
        type: CustomPacketPayloadType<T>,
        handler: (payload: T, context: Context) -> Unit
    ): ServerPlayNetworking.PlayChannelHandler {
        return { _, player, _, buf, _ -> handler(type.read(buf), Context(player)) }
    }
}