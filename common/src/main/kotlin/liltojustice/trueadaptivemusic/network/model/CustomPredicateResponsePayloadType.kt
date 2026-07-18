package liltojustice.trueadaptivemusic.network.model

import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceLocation

object CustomPredicateResponsePayloadType
    : CustomPacketPayloadType<CustomPredicateResponsePayloadType.CustomPredicateResponsePayload> {
    override val identifier = ResourceLocation("trueadaptivemusic", "custom_predicate_response")

    data class CustomPredicateResponsePayload(
        val predicateId: String, val predicateResponse: Boolean): CustomPacketPayload

    override fun read(buf: FriendlyByteBuf): CustomPredicateResponsePayload {
        return CustomPredicateResponsePayload(buf.readUtf(), buf.readBoolean())
    }

    override fun write(payload: CustomPredicateResponsePayload, buf: FriendlyByteBuf) {
        buf.writeUtf(payload.predicateId)
        buf.writeBoolean(payload.predicateResponse)
    }
}
