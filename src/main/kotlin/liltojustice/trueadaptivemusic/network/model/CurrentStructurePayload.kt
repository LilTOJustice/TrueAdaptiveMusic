package liltojustice.trueadaptivemusic.network.model

import net.fabricmc.fabric.api.networking.v1.FabricPacket
import net.fabricmc.fabric.api.networking.v1.PacketType
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.Identifier

data class CurrentStructurePayload(
    val structureIdentifier: Identifier,
    val structureSetIdentifier: Identifier,
    val structurePieceIdentifier: Identifier
): FabricPacket {
    constructor(buf: PacketByteBuf): this(
        buf.readIdentifier(), buf.readIdentifier(), buf.readIdentifier())

    override fun write(buf: PacketByteBuf) {
        buf.writeIdentifier(structureIdentifier)
        buf.writeIdentifier(structureSetIdentifier)
    }

    override fun getType(): PacketType<*> {
        return TYPE
    }

    companion object {
        val ID = Identifier.of("trueadaptivemusic", "structure_payload")!!
        val TYPE: PacketType<CurrentStructurePayload> = PacketType.create(ID, ::CurrentStructurePayload)
    }
}
