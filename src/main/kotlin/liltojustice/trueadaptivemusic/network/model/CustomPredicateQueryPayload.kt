package liltojustice.trueadaptivemusic.network.model

import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier

data class CustomPredicateQueryPayload(val predicateId: String, val predicateText: String): CustomPayload {
    override fun getId(): CustomPayload.Id<out CustomPayload?> {
        return ID
    }

    companion object {
        val ID: CustomPayload.Id<CustomPredicateQueryPayload> = CustomPayload.Id(
            Identifier.of("trueadaptivemusic", "custom_predicate_query"))
        val CODEC: PacketCodec<ByteBuf, CustomPredicateQueryPayload> = PacketCodec.tuple(
            PacketCodecs.string(MAX_CUSTOM_PREDICATE_ID_LENGTH),
            CustomPredicateQueryPayload::predicateId,
            PacketCodecs.string(MAX_CUSTOM_PREDICATE_LENGTH),
            CustomPredicateQueryPayload::predicateText,
            ::CustomPredicateQueryPayload
        )
        const val MAX_CUSTOM_PREDICATE_ID_LENGTH = 1000
        const val MAX_CUSTOM_PREDICATE_LENGTH = 10000
    }
}
