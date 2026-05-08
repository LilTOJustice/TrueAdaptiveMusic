package liltojustice.trueadaptivemusic.network.model

import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier

data class CurrentStructurePayload(
    val structureIdentifier: Identifier,
    val structureSetIdentifier: Identifier,
    val structurePieceIdentifier: Identifier
): CustomPayload {
    override fun getId(): CustomPayload.Id<out CustomPayload?> {
        return ID
    }

    companion object {
        val ID: CustomPayload.Id<CurrentStructurePayload> = CustomPayload.Id(
            Identifier.of("trueadaptivemusic", "structure_payload"))
        val CODEC: PacketCodec<ByteBuf, CurrentStructurePayload> = PacketCodec.tuple(
            Identifier.PACKET_CODEC,
            CurrentStructurePayload::structureIdentifier,
            Identifier.PACKET_CODEC,
            CurrentStructurePayload::structureSetIdentifier,
            Identifier.PACKET_CODEC,
            CurrentStructurePayload::structurePieceIdentifier,
            ::CurrentStructurePayload
        )
    }
}
