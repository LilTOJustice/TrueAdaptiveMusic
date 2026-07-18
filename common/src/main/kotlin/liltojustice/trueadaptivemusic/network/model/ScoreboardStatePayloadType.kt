package liltojustice.trueadaptivemusic.network.model

import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceLocation

object ScoreboardStatePayloadType: CustomPacketPayloadType<ScoreboardStatePayloadType.ScoreboardStatePayload> {
    override val identifier = ResourceLocation("trueadaptivemusic", "scoreboard_payload")

    data class ScoreboardStatePayload(val objectiveName: String, val value: Int): CustomPacketPayload

    override fun read(buf: FriendlyByteBuf): ScoreboardStatePayload {
        return ScoreboardStatePayload(buf.readUtf(), buf.readInt())
    }

    override fun write(payload: ScoreboardStatePayload, buf: FriendlyByteBuf) {
        buf.writeUtf(payload.objectiveName)
        buf.writeInt(payload.value)
    }
}
