package liltojustice.trueadaptivemusic.network.model

import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier
import net.minecraft.world.WorldProperties

data class SpawnPointPayload(val spawnPoint: WorldProperties.SpawnPoint): CustomPayload {
    override fun getId(): CustomPayload.Id<out CustomPayload?> {
        return ID
    }

    companion object {
        val ID: CustomPayload.Id<SpawnPointPayload> = CustomPayload.Id(
            Identifier.of("trueadaptivemusic", "spawn_point"))
        val CODEC: PacketCodec<ByteBuf, SpawnPointPayload> = PacketCodec.tuple(
            WorldProperties.SpawnPoint.PACKET_CODEC,
            SpawnPointPayload::spawnPoint,
            ::SpawnPointPayload
        )
    }
}
