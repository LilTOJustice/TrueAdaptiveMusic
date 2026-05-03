package liltojustice.trueadaptivemusic.network.model

import net.fabricmc.fabric.api.networking.v1.FabricPacket
import net.fabricmc.fabric.api.networking.v1.PacketType
import net.minecraft.network.PacketByteBuf
import net.minecraft.registry.RegistryKeys
import net.minecraft.util.Identifier

data class SpawnPointPayload(val spawnPoint: SpawnPoint): FabricPacket {
    constructor(buf: PacketByteBuf):
            this(
                SpawnPoint(
                    buf.readBlockPos(), buf.readRegistryKey(RegistryKeys.WORLD))
            )

    override fun write(buf: PacketByteBuf) {
        buf.writeBlockPos(spawnPoint.blockPos)
        buf.writeRegistryKey(spawnPoint.dimension)
    }

    override fun getType(): PacketType<*> {
        return TYPE
    }

    companion object {
        val ID = Identifier.of("trueadaptivemusic", "spawn_point")!!
        val TYPE: PacketType<SpawnPointPayload> = PacketType.create(ID, ::SpawnPointPayload)
    }
}
