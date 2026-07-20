package liltojustice.trueadaptivemusic.network.model

import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceLocation

object CurrentStructurePayloadType: CustomPacketPayloadType<CurrentStructurePayloadType.CurrentStructurePayload> {
    override val identifier = ResourceLocation("trueadaptivemusic", "structure_payload")
    override val payloadClass = CurrentStructurePayload::class

    data class CurrentStructurePayload(
        val structureIdentifier: ResourceLocation,
        val structureSetIdentifier: ResourceLocation,
        val structurePieceIdentifier: ResourceLocation
    ): CustomPacketPayload

    override fun read(buf: FriendlyByteBuf): CurrentStructurePayload {
        return CurrentStructurePayload(
            buf.readResourceLocation(),
            buf.readResourceLocation(),
            buf.readResourceLocation()
        )
    }

    override fun write(payload: CurrentStructurePayload, buf: FriendlyByteBuf) {
        buf.writeResourceLocation(payload.structureIdentifier)
        buf.writeResourceLocation(payload.structureSetIdentifier)
        buf.writeResourceLocation(payload.structurePieceIdentifier)
    }
}
