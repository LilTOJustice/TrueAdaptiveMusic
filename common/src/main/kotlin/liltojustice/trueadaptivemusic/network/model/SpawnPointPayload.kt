package liltojustice.trueadaptivemusic.network.model

import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation

data class SpawnPointPayload(val spawnPoint: SpawnPoint): CustomPacketPayload {
    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> {
        return ID
    }

    companion object {
        val ID: CustomPacketPayload.Type<SpawnPointPayload> = CustomPacketPayload.Type(
            ResourceLocation.fromNamespaceAndPath("trueadaptivemusic", "spawn_point"))
        val CODEC: StreamCodec<ByteBuf, SpawnPointPayload> = StreamCodec.composite(
            SpawnPoint.PACKET_CODEC,
            SpawnPointPayload::spawnPoint,
            ::SpawnPointPayload
        )
    }
}
