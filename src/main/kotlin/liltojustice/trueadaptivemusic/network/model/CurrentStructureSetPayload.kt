package liltojustice.trueadaptivemusic.network.model

import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.Identifier

data class CurrentStructureSetPayload(val structureSetIdentifier: Identifier): CustomPacketPayload {
    override fun type(): CustomPacketPayload.Type<out CurrentStructureSetPayload> {
        return TYPE
    }

    companion object {
        val ID = Identifier.fromNamespaceAndPath("trueadaptivemusic", "structure_set_payload")
        val TYPE = CustomPacketPayload.Type<CurrentStructureSetPayload>(ID)
        val CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC,
            CurrentStructureSetPayload::structureSetIdentifier,
            ::CurrentStructureSetPayload
        )
    }
}
