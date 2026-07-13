package liltojustice.trueadaptivemusic.network

import io.netty.buffer.ByteBuf
import liltojustice.trueadaptivemusic.network.model.Context
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.server.level.ServerPlayer
import net.neoforged.neoforge.network.PacketDistributor
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent
import net.neoforged.neoforge.network.registration.PayloadRegistrar

object NeoforgeServerNetworkingInterface: ServerNetworkInterface {
    private const val REGISTRAR_VERSION = "1"
    private val registrations = mutableListOf<(PayloadRegistrar) -> Unit>()

    fun registerPayloadHandlers(event: RegisterPayloadHandlersEvent) {
        val registrar = event.registrar(REGISTRAR_VERSION)
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

    override fun sendToClient(player: ServerPlayer, payload: CustomPacketPayload) {
        PacketDistributor.sendToPlayer(player, payload)
    }
}