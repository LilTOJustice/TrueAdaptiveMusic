package liltojustice.trueadaptivemusic.network.model

import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.Identifier

data class CustomPredicateResponsePayload(val predicateId: String, val predicateResponse: Boolean): CustomPacketPayload {
    override fun type(): CustomPacketPayload.Type<out CustomPredicateResponsePayload> {
        return TYPE
    }

    companion object {
        val ID = Identifier.fromNamespaceAndPath("trueadaptivemusic", "custom_predicate_response")
        val TYPE = CustomPacketPayload.Type<CustomPredicateResponsePayload>(ID)
        val CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            CustomPredicateResponsePayload::predicateId,
            ByteBufCodecs.BOOL,
            CustomPredicateResponsePayload::predicateResponse,
            ::CustomPredicateResponsePayload
        )
    }
}
