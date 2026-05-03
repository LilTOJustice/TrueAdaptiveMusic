package liltojustice.trueadaptivemusic.network.model

import net.fabricmc.fabric.api.networking.v1.FabricPacket
import net.fabricmc.fabric.api.networking.v1.PacketType
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.Identifier

data class CustomPredicateResponsePayload(val predicateId: String, val predicateResponse: Boolean): FabricPacket {
    constructor(buf: PacketByteBuf): this(buf.readString(), buf.readBoolean())

    override fun write(buf: PacketByteBuf) {
        buf.writeString(predicateId)
        buf.writeBoolean(predicateResponse)
    }

    override fun getType(): PacketType<*> {
        return TYPE
    }

    companion object {
        val ID = Identifier.of("trueadaptivemusic", "custom_predicate_response")!!
        val TYPE: PacketType<CustomPredicateResponsePayload> = PacketType.create(ID, ::CustomPredicateResponsePayload)
    }
}
