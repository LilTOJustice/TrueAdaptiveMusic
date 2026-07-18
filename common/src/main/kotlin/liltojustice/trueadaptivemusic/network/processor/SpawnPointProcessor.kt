package liltojustice.trueadaptivemusic.network.processor

import liltojustice.trueadaptivemusic.network.model.SpawnPoint
import liltojustice.trueadaptivemusic.network.model.SpawnPointPayloadType
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer

class SpawnPointProcessor: Processor<SpawnPointPayloadType.SpawnPointPayload>() {
    val spawnCache = mutableMapOf<ServerPlayer, SpawnPoint>()
    override fun makePacket(
        server: MinecraftServer,
        player: ServerPlayer
    ): ProcessorResult<SpawnPointPayloadType.SpawnPointPayload>? {
        val spawnPosition = player.respawnPosition ?: return null
        val spawnDimension = player.respawnDimension ?: return null
        val spawnPoint = SpawnPoint(spawnPosition, spawnDimension)
        val cached = spawnCache[player]
        if (cached?.blockPos != spawnPosition || cached.dimension != spawnDimension) {
            spawnCache[player] = spawnPoint

            return ProcessorResult(SpawnPointPayloadType, SpawnPointPayloadType.SpawnPointPayload(spawnPoint))
        }

        return null
    }
}