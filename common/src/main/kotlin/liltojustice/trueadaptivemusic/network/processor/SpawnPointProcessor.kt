package liltojustice.trueadaptivemusic.network.processor

import liltojustice.trueadaptivemusic.network.model.SpawnPoint
import liltojustice.trueadaptivemusic.network.model.SpawnPointPayload
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer

class SpawnPointProcessor: Processor() {
    val spawnCache = mutableMapOf<ServerPlayer, SpawnPoint>()
    override fun makePacket(server: MinecraftServer, player: ServerPlayer): CustomPacketPayload? {
        val spawnPosition = player.respawnPosition ?: return null
        val spawnDimension = player.respawnDimension ?: return null
        val spawnPoint = SpawnPoint(spawnPosition, spawnDimension)
        val cached = spawnCache[player]
        if (cached?.blockPos != spawnPosition || cached.dimension != spawnDimension) {
            spawnCache[player] = spawnPoint

            return SpawnPointPayload(spawnPoint)
        }

        return null
    }
}