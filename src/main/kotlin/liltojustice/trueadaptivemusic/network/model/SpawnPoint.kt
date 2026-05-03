package liltojustice.trueadaptivemusic.network.model

import net.minecraft.network.codec.PacketCodec
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

data class SpawnPoint(val blockPos: BlockPos, val dimension: RegistryKey<World>) {
    companion object {
        val PACKET_CODEC = PacketCodec.tuple(
            BlockPos.PACKET_CODEC,
            SpawnPoint::blockPos,
            RegistryKey.createPacketCodec(RegistryKeys.WORLD),
            SpawnPoint::dimension,
            ::SpawnPoint
        )
    }
}
