package liltojustice.trueadaptivemusic.network.model

import net.fabricmc.fabric.api.networking.v1.FabricPacket
import net.fabricmc.fabric.api.networking.v1.PacketType
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.Identifier

data class CustomPredicateQueryPayload(val predicateId: String, val predicateText: String): FabricPacket {
    constructor(buf: PacketByteBuf): this(buf.readString(), buf.readString())

    override fun write(buf: PacketByteBuf) {
        buf.writeString(predicateId)
        buf.writeString(predicateText)
    }

    override fun getType(): PacketType<*> {
        return TYPE
    }

    companion object {
        val ID = Identifier.of("trueadaptivemusic", "custom_predicate_query")!!
        val TYPE: PacketType<CustomPredicateQueryPayload> =
            PacketType.create(ID, ::CustomPredicateQueryPayload)
    }
}
