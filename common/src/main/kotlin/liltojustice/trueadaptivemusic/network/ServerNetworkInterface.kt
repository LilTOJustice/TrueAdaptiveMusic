package liltojustice.trueadaptivemusic.network

import liltojustice.trueadaptivemusic.network.model.Context
import liltojustice.trueadaptivemusic.network.model.CustomPacketPayload
import liltojustice.trueadaptivemusic.network.model.CustomPacketPayloadType
import net.minecraft.server.level.ServerPlayer

@Suppress("UNUSED")
interface ServerNetworkInterface {
    fun <T: CustomPacketPayload> registerServerboundPacket(
        type: CustomPacketPayloadType<T>, handler: (payload: T, context: Context) -> Unit)

    fun <T: CustomPacketPayload> sendToClient(player: ServerPlayer, type: CustomPacketPayloadType<T>, payload: T)
}