package liltojustice.trueadaptivemusic.network.model

import net.minecraft.network.codec.StreamCodec
import net.minecraft.resources.ResourceKey
import net.minecraft.core.registries.Registries
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level

data class SpawnPoint(val blockPos: BlockPos, val dimension: ResourceKey<Level>) {
    companion object {
        val PACKET_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            SpawnPoint::blockPos,
            ResourceKey.streamCodec(Registries.DIMENSION),
            SpawnPoint::dimension,
            ::SpawnPoint
        )
    }
}
