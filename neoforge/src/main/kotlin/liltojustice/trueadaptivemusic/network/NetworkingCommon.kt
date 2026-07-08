package liltojustice.trueadaptivemusic.network

import io.netty.buffer.ByteBuf
import liltojustice.trueadaptivemusic.common.client.network.TAMClientNetworking
import liltojustice.trueadaptivemusic.common.network.TAMServerNetworking
import liltojustice.trueadaptivemusic.common.network.model.Context
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.server.level.ServerPlayer
import net.neoforged.neoforge.client.network.ClientPacketDistributor
import net.neoforged.neoforge.network.PacketDistributor
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent
import net.neoforged.neoforge.network.handling.IPayloadContext
import net.neoforged.neoforge.network.handling.IPayloadHandler

object NetworkingCommon {
    private const val REGISTRAR_VERSION = "1"
    private var payloadHandlersEvent: RegisterPayloadHandlersEvent? = null

    fun registerPayloadHandlers(event: RegisterPayloadHandlersEvent) {
        payloadHandlersEvent = event
        TAMClientNetworking.init()
        TAMServerNetworking.init()
    }

    @Suppress("UNUSED")
    fun <T: CustomPacketPayload> registerServerboundPacket(
        type: CustomPacketPayload.Type<T>,
        codec: StreamCodec<ByteBuf, T>,
        handler: (payload: T, context: Context) -> Unit
    ) {
        val registrar = payloadHandlersEvent?.registrar(REGISTRAR_VERSION) ?: return
        registrar.playToServer(type, codec, transformHandler(handler))
    }

    @Suppress("UNUSED")
    fun <T: CustomPacketPayload> registerClientboundPacket(
        type: CustomPacketPayload.Type<T>,
        codec: StreamCodec<ByteBuf, T>,
        handler: (payload: T, context: Context) -> Unit
    ) {
        val registrar = payloadHandlersEvent?.registrar(REGISTRAR_VERSION) ?: return
        registrar.playToClient(type, codec, transformHandler(handler))
    }

    @Suppress("UNUSED")
    fun sendToServer(payload: CustomPacketPayload) {
        ClientPacketDistributor.sendToServer(payload)
    }

    @Suppress("UNUSED")
    fun sendToClient(player: ServerPlayer, payload: CustomPacketPayload) {
        PacketDistributor.sendToPlayer(player, payload)
    }

    private fun <T: CustomPacketPayload> transformHandler(
        handler: (payload: T, context: Context) -> Unit): IPayloadHandler<T> {
        return { payload, context -> handler(payload, context.toCommonContext()) }
    }

    private fun IPayloadContext.toCommonContext(): Context {
        return Context(this.player())
    }
}