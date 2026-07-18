package liltojustice.trueadaptivemusic.network.model

import net.minecraft.network.FriendlyByteBuf
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation

object SpawnPointPayloadType: CustomPacketPayloadType<SpawnPointPayloadType.SpawnPointPayload> {
    override val identifier = ResourceLocation("trueadaptivemusic", "spawn_point")

    data class SpawnPointPayload(val spawnPoint: SpawnPoint): CustomPacketPayload

    override fun read(buf: FriendlyByteBuf): SpawnPointPayload {
        return SpawnPointPayload(
            SpawnPoint(buf.readBlockPos(), buf.readResourceKey(Registries.DIMENSION)))
    }

    override fun write(payload: SpawnPointPayload, buf: FriendlyByteBuf) {
        val spawnPoint = payload.spawnPoint
        buf.writeBlockPos(spawnPoint.blockPos)
        buf.writeResourceKey(spawnPoint.dimension)
    }
}
