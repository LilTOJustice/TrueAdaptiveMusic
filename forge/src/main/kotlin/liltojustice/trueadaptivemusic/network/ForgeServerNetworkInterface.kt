package liltojustice.trueadaptivemusic.network

import liltojustice.trueadaptivemusic.network.model.Context
import liltojustice.trueadaptivemusic.network.model.CustomPacketPayload
import liltojustice.trueadaptivemusic.network.model.CustomPacketPayloadType
import net.minecraft.server.level.ServerPlayer
import net.minecraftforge.network.PacketDistributor

object ForgeServerNetworkInterface: ServerNetworkInterface {
    override fun <T : CustomPacketPayload> registerServerboundPacket(
        type: CustomPacketPayloadType<T>,
        handler: (payload: T, context: Context) -> Unit
    ) {
        CommonNetworkingInterface.registerToServer(type, handler)
    }

    override fun <T : CustomPacketPayload> sendToClient(
        player: ServerPlayer,
        type: CustomPacketPayloadType<T>,
        payload: T
    ) {
        CommonNetworkingInterface.channel.send(PacketDistributor.PLAYER.with { player }, payload)
    }
}