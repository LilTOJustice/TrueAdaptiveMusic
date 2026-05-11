package liltojustice.trueadaptivemusic.network.model

import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier

data class ScoreboardStatePayload(val objectiveName: String, val value: Int): CustomPayload {
    override fun getId(): CustomPayload.Id<out CustomPayload?> {
        return ID
    }

    companion object {
        val ID: CustomPayload.Id<ScoreboardStatePayload> = CustomPayload.Id(
            Identifier.of("trueadaptivemusic", "scoreboard_payload"))
        val CODEC: PacketCodec<ByteBuf, ScoreboardStatePayload> = PacketCodec.tuple(
            PacketCodecs.STRING,
            ScoreboardStatePayload::objectiveName,
            PacketCodecs.INTEGER,
            ScoreboardStatePayload::value,
            ::ScoreboardStatePayload
        )
    }
}
