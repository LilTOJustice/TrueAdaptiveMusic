package liltojustice.trueadaptivemusic.network.model

import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.Identifier

data class ScoreboardStatePayload(val objectiveName: String, val value: Int): CustomPacketPayload {
    override fun type(): CustomPacketPayload.Type<out ScoreboardStatePayload> {
        return TYPE
    }

    companion object {
        val ID = Identifier.fromNamespaceAndPath("trueadaptivemusic", "scoreboard_payload")
        val TYPE = CustomPacketPayload.Type<ScoreboardStatePayload>(ID)
        val CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            ScoreboardStatePayload::objectiveName,
            ByteBufCodecs.INT,
            ScoreboardStatePayload::value,
            ::ScoreboardStatePayload
        )
    }
}
