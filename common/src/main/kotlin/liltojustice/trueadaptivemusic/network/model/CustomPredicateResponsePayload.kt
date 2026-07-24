package liltojustice.trueadaptivemusic.network.model

import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation

data class CustomPredicateResponsePayload(val predicateId: String, val predicateResponse: Boolean):
    CustomPacketPayload {
    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> {
        return ID
    }

    companion object {
        val ID: CustomPacketPayload.Type<CustomPredicateResponsePayload> = CustomPacketPayload.Type(
            ResourceLocation.fromNamespaceAndPath("trueadaptivemusic", "custom_predicate_response"))
        val CODEC: StreamCodec<ByteBuf, CustomPredicateResponsePayload> = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            CustomPredicateResponsePayload::predicateId,
            ByteBufCodecs.BOOL,
            CustomPredicateResponsePayload::predicateResponse,
            ::CustomPredicateResponsePayload
        )
    }
}
