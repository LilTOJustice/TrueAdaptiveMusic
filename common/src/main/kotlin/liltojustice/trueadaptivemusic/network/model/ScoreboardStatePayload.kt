package liltojustice.trueadaptivemusic.network.model

import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation

data class ScoreboardStatePayload(val objectiveName: String, val value: Int): CustomPacketPayload {
    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> {
        return TYPE
    }

    companion object {
        val TYPE: CustomPacketPayload.Type<ScoreboardStatePayload> = CustomPacketPayload.Type(
            ResourceLocation.fromNamespaceAndPath("trueadaptivemusic", "scoreboard_payload"))
        val CODEC: StreamCodec<ByteBuf, ScoreboardStatePayload> = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            ScoreboardStatePayload::objectiveName,
            ByteBufCodecs.INT,
            ScoreboardStatePayload::value,
            ::ScoreboardStatePayload
        )
    }
}
