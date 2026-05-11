package liltojustice.trueadaptivemusic.network.model

import net.fabricmc.fabric.api.networking.v1.FabricPacket
import net.fabricmc.fabric.api.networking.v1.PacketType
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.Identifier

data class ScoreboardStatePayload(val objectiveName: String, val value: Int): FabricPacket {
    constructor(buf: PacketByteBuf): this(buf.readString(), buf.readInt())

    override fun write(buf: PacketByteBuf) {
        buf.writeString(objectiveName)
        buf.writeInt(value)
    }

    override fun getType(): PacketType<*> {
        return TYPE
    }

    companion object {
        val ID = Identifier.of("trueadaptivemusic", "scoreboard_payload")!!
        val TYPE: PacketType<ScoreboardStatePayload> = PacketType.create(ID, ::ScoreboardStatePayload)
    }
}
