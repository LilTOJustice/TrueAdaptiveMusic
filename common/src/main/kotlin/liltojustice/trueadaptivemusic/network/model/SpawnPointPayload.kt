package liltojustice.trueadaptivemusic.network.model

import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.storage.LevelData

data class SpawnPointPayload(val spawnPoint: LevelData.RespawnData): CustomPacketPayload {
    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> {
        return TYPE
    }

    companion object {
        val TYPE: CustomPacketPayload.Type<SpawnPointPayload> = CustomPacketPayload.Type(
            ResourceLocation.fromNamespaceAndPath("trueadaptivemusic", "spawn_point"))
        val CODEC: StreamCodec<ByteBuf, SpawnPointPayload> = StreamCodec.composite(
            LevelData.RespawnData.STREAM_CODEC,
            SpawnPointPayload::spawnPoint,
            ::SpawnPointPayload
        )
    }
}
