package liltojustice.trueadaptivemusic.common.network.model

import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.Identifier

data class CustomPredicateQueryPayload(val predicateId: String, val predicateText: String): CustomPacketPayload {
    override fun type(): CustomPacketPayload.Type<out CustomPredicateQueryPayload> {
        return TYPE
    }

    companion object {
        val ID = Identifier.fromNamespaceAndPath("trueadaptivemusic", "custom_predicate_query")
        val TYPE = CustomPacketPayload.Type<CustomPredicateQueryPayload>(ID)
        val CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            CustomPredicateQueryPayload::predicateId,
            ByteBufCodecs.STRING_UTF8,
            CustomPredicateQueryPayload::predicateText,
            ::CustomPredicateQueryPayload
        )
    }
}
