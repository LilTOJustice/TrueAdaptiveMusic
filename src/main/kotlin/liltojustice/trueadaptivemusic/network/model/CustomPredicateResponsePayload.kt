package liltojustice.trueadaptivemusic.network.model

import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier

data class CustomPredicateResponsePayload(val predicateId: String, val predicateResponse: Boolean): CustomPayload {
    override fun getId(): CustomPayload.Id<out CustomPayload?> {
        return ID
    }

    companion object {
        val ID: CustomPayload.Id<CustomPredicateResponsePayload> = CustomPayload.Id(
            Identifier.of("trueadaptivemusic", "custom_predicate_response"))
        val CODEC: PacketCodec<ByteBuf, CustomPredicateResponsePayload> = PacketCodec.tuple(
            PacketCodecs.string(
                CustomPredicateQueryPayload.MAX_CUSTOM_PREDICATE_ID_LENGTH),
            CustomPredicateResponsePayload::predicateId,
            PacketCodecs.BOOL,
            CustomPredicateResponsePayload::predicateResponse,
            ::CustomPredicateResponsePayload
        )
    }
}
