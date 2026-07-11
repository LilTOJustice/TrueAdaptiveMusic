package liltojustice.trueadaptivemusic.network.model

import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.Identifier
import net.minecraft.world.level.storage.LevelData

data class SpawnPointPayload(val spawnPoint: LevelData.RespawnData): CustomPacketPayload {
    override fun type(): CustomPacketPayload.Type<out SpawnPointPayload> {
        return TYPE
    }

    companion object {
        val ID = Identifier.fromNamespaceAndPath("trueadaptivemusic", "spawn_point")
        val TYPE = CustomPacketPayload.Type<SpawnPointPayload>(ID)
        val CODEC = StreamCodec.composite(
            LevelData.RespawnData.STREAM_CODEC,
            SpawnPointPayload::spawnPoint,
            ::SpawnPointPayload
        )
    }
}
