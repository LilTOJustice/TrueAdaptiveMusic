package liltojustice.trueadaptivemusic.network.processor

import liltojustice.trueadaptivemusic.network.model.SpawnPoint
import liltojustice.trueadaptivemusic.network.model.SpawnPointPayload
import net.fabricmc.fabric.api.networking.v1.FabricPacket
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity

class SpawnPointProcessor: Processor() {
    val spawnCache = mutableMapOf<ServerPlayerEntity, SpawnPoint>()
    override fun makePacket(server: MinecraftServer, player: ServerPlayerEntity): FabricPacket? {
        val spawnPosition = player.spawnPointPosition ?: return null
        val spawnDimension = player.spawnPointDimension ?: return null
        val spawnPoint = SpawnPoint(spawnPosition, spawnDimension)
        val cached = spawnCache[player]
        if (cached?.blockPos != spawnPosition || cached.dimension != spawnDimension) {
            spawnCache[player] = spawnPoint

            return SpawnPointPayload(spawnPoint)
        }

        return null
    }
}