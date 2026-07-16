package liltojustice.trueadaptivemusic.network.model

import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation

data class CustomPredicateQueryPayload(val predicateId: String, val predicateText: String): CustomPacketPayload {
    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> {
        return ID
    }

    companion object {
        val ID: CustomPacketPayload.Type<CustomPredicateQueryPayload> = CustomPacketPayload.Type(
            ResourceLocation.fromNamespaceAndPath("trueadaptivemusic", "custom_predicate_query"))
        val CODEC: StreamCodec<ByteBuf, CustomPredicateQueryPayload> = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            CustomPredicateQueryPayload::predicateId,
            ByteBufCodecs.STRING_UTF8,
            CustomPredicateQueryPayload::predicateText,
            ::CustomPredicateQueryPayload
        )
    }
}
