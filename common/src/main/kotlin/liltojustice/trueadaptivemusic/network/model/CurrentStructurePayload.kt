package liltojustice.trueadaptivemusic.network.model

import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.Identifier

data class CurrentStructurePayload(
    val structureIdentifier: Identifier,
    val structureSetIdentifier: Identifier,
    val structurePieceIdentifier: Identifier
): CustomPacketPayload {
    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> {
        return TYPE
    }

    companion object {
        val TYPE: CustomPacketPayload.Type<CurrentStructurePayload> = CustomPacketPayload.Type(
            Identifier.fromNamespaceAndPath("trueadaptivemusic", "structure_payload"))
        val CODEC: StreamCodec<ByteBuf, CurrentStructurePayload> = StreamCodec.composite(
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
