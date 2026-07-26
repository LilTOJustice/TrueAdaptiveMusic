package liltojustice.trueadaptivemusic.network

import io.netty.buffer.ByteBuf
import liltojustice.trueadaptivemusic.network.model.Context
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.server.level.ServerPlayer

@Suppress("UNUSED")
interface ServerNetworkInterface {
    fun <T: CustomPacketPayload> registerServerboundPacket(
        type: CustomPacketPayload.Type<T>,
        codec: StreamCodec<ByteBuf, T>,
        handler: (payload: T, context: Context) -> Unit
    )

    fun <T: CustomPacketPayload> registerClientboundPacket(
        type: CustomPacketPayload.Type<T>,
        codec: StreamCodec<ByteBuf, T>,
    )

    fun sendToClient(player: ServerPlayer, payload: CustomPacketPayload)
}