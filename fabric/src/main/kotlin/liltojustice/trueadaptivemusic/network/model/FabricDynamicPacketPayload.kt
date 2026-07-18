package liltojustice.trueadaptivemusic.network.model

import net.fabricmc.fabric.api.networking.v1.FabricPacket
import net.fabricmc.fabric.api.networking.v1.PacketType
import net.minecraft.network.FriendlyByteBuf

class FabricDynamicPacketPayload<T: CustomPacketPayload>(
    val type: CustomPacketPayloadType<T>, val payload: T): FabricPacket {
    private val packetType = PacketType<FabricDynamicPacketPayload<T>>
        .create(type.identifier, ::create)

    private fun create(buf: FriendlyByteBuf): FabricPacket {
        return FabricDynamicPacketPayload(type, type.read(buf))
    }

    override fun write(buf: FriendlyByteBuf) {
        type.write(payload, buf)
    }

    override fun getType(): PacketType<*> {
        return packetType
    }
}