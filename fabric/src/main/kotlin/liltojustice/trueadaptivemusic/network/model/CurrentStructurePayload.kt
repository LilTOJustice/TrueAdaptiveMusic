package liltojustice.trueadaptivemusic.network.model

import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.Identifier

data class CurrentStructurePayload(
    val structureIdentifier: Identifier,
    val structureSetIdentifier: Identifier,
    val structurePieceIdentifier: Identifier
): CustomPacketPayload {
    override fun type(): CustomPacketPayload.Type<out CurrentStructurePayload> {
        return TYPE
    }

    companion object {
        val ID = Identifier.fromNamespaceAndPath("trueadaptivemusic", "structure_payload")
        val TYPE = CustomPacketPayload.Type<CurrentStructurePayload>(ID)
        val CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC,
            CurrentStructurePayload::structureIdentifier,
            Identifier.STREAM_CODEC,
            CurrentStructurePayload::structureSetIdentifier,
            Identifier.STREAM_CODEC,
            CurrentStructurePayload::structurePieceIdentifier,
            ::CurrentStructurePayload
        )
    }
}
