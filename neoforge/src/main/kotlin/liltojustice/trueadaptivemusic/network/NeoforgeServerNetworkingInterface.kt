package liltojustice.trueadaptivemusic.network

import io.netty.buffer.ByteBuf
import liltojustice.trueadaptivemusic.network.model.Context
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.server.level.ServerPlayer
import net.neoforged.neoforge.network.PacketDistributor
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent
import net.neoforged.neoforge.network.registration.PayloadRegistrar

class NeoforgeServerNetworkingInterface(private val isDedicatedServer: Boolean): ServerNetworkInterface {
    private val registrarVersion = "1"
    private val registrations = mutableListOf<(PayloadRegistrar) -> Unit>()

    fun registerPayloadHandlers(event: RegisterPayloadHandlersEvent) {
        val registrar = event.registrar(registrarVersion)
        registrations.forEach { it(registrar) }
    }

    override fun <T: CustomPacketPayload> registerServerboundPacket(
        type: CustomPacketPayload.Type<T>,
        codec: StreamCodec<ByteBuf, T>,
        handler: (payload: T, context: Context) -> Unit
    ) {
        registrations.add { registrar ->
            registrar.playToServer(type, codec, transformHandler(handler))
        }
    }

    override fun <T : CustomPacketPayload> registerClientboundPacket(
        type: CustomPacketPayload.Type<T>,
        codec: StreamCodec<ByteBuf, T>
    ) {
        if (isDedicatedServer) {
            registrations.add { registrar -> registrar.playToClient(type, codec) { _, _ -> } }
        }
    }

    override fun sendToClient(player: ServerPlayer, payload: CustomPacketPayload) {
        PacketDistributor.sendToPlayer(player, payload)
    }
}