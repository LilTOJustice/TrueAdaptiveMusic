package liltojustice.trueadaptivemusic.network.model

import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation

data class CurrentStructurePayload(
    val structureIdentifier: ResourceLocation,
    val structureSetIdentifier: ResourceLocation,
    val structurePieceIdentifier: ResourceLocation
): CustomPacketPayload {
    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> {
        return TYPE
    }

    companion object {
        val TYPE: CustomPacketPayload.Type<CurrentStructurePayload> = CustomPacketPayload.Type(
            ResourceLocation.fromNamespaceAndPath("trueadaptivemusic", "structure_payload"))
        val CODEC: StreamCodec<ByteBuf, CurrentStructurePayload> = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC,
            CurrentStructurePayload::structureIdentifier,
            ResourceLocation.STREAM_CODEC,
            CurrentStructurePayload::structureSetIdentifier,
            ResourceLocation.STREAM_CODEC,
            CurrentStructurePayload::structurePieceIdentifier,
            ::CurrentStructurePayload
        )
    }
}
