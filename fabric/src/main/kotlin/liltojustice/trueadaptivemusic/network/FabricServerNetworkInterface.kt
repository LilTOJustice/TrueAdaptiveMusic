package liltojustice.trueadaptivemusic.network

import io.netty.buffer.ByteBuf
import liltojustice.trueadaptivemusic.network.model.Context
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.server.level.ServerPlayer

object FabricServerNetworkInterface: ServerNetworkInterface {
    override fun <T: CustomPacketPayload> registerServerboundPacket(
        type: CustomPacketPayload.Type<T>,
        codec: StreamCodec<ByteBuf, T>,
        handler: (payload: T, context: Context) -> Unit
    ) {
        PayloadTypeRegistry.playC2S().register(type, codec)
        ServerPlayNetworking.registerGlobalReceiver(type, transformHandlerServer(handler))
    }

    override fun <T : CustomPacketPayload> registerClientboundPacket(
        type: CustomPacketPayload.Type<T>,
        codec: StreamCodec<ByteBuf, T>
    ) {
        PayloadTypeRegistry.playS2C().register(type, codec)
    }

    override fun sendToClient(player: ServerPlayer, payload: CustomPacketPayload) {
        ServerPlayNetworking.send(player, payload)
    }

    private fun <T: CustomPacketPayload> transformHandlerServer(
        handler: (payload: T, context: Context) -> Unit): ServerPlayNetworking.PlayPayloadHandler<T> {
        return { payload, context -> handler(payload, context.toCommonContext()) }
    }

    private fun ServerPlayNetworking.Context.toCommonContext(): Context {
        return Context(this.player())
    }
}