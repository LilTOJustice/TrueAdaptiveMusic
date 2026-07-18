package liltojustice.trueadaptivemusic.network.model

import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceLocation

object CustomPredicateQueryPayloadType
    : CustomPacketPayloadType<CustomPredicateQueryPayloadType.CustomPredicateQueryPayload> {
    override val identifier = ResourceLocation("trueadaptivemusic", "custom_predicate_query")

    data class CustomPredicateQueryPayload(val predicateId: String, val predicateText: String): CustomPacketPayload

    override fun read(buf: FriendlyByteBuf): CustomPredicateQueryPayload {
        return CustomPredicateQueryPayload(buf.readUtf(), buf.readUtf())
    }

    override fun write(payload: CustomPredicateQueryPayload, buf: FriendlyByteBuf) {
        buf.writeUtf(payload.predicateId)
        buf.writeUtf(payload.predicateText)
    }
}
