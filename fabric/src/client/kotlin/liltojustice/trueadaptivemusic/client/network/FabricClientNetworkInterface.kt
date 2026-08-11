package liltojustice.trueadaptivemusic.client.network

import io.netty.buffer.ByteBuf
import liltojustice.trueadaptivemusic.network.ClientNetworkInterface
import liltojustice.trueadaptivemusic.network.model.Context
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload

object FabricClientNetworkInterface: ClientNetworkInterface {
    override fun <T: CustomPacketPayload> registerClientboundPacket(
        type: CustomPacketPayload.Type<T>,
        codec: StreamCodec<ByteBuf, T>,
        handler: (payload: T, context: Context) -> Unit
    ) {
        ClientPlayNetworking.registerGlobalReceiver(type,  transformHandlerClient(handler))
    }

    override fun <T: CustomPacketPayload> registerServerboundPacket(
        type: CustomPacketPayload.Type<T>,
        codec: StreamCodec<ByteBuf, T>
    ) {
    }

    override fun sendToServer(payload: CustomPacketPayload) {
        ClientPlayNetworking.send(payload)
    }

    private fun <T: CustomPacketPayload> transformHandlerClient(
        handler: (payload: T, context: Context) -> Unit): ClientPlayNetworking.PlayPayloadHandler<T> {
        return { payload, context -> handler(payload, context.toCommonContext()) }
    }

    private fun ClientPlayNetworking.Context.toCommonContext(): Context {
        return Context(this.player())
    }
}