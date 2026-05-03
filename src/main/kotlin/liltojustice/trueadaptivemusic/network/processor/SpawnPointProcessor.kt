package liltojustice.trueadaptivemusic.network.processor

import liltojustice.trueadaptivemusic.network.model.SpawnPoint
import liltojustice.trueadaptivemusic.network.model.SpawnPointPayload
import net.minecraft.network.packet.CustomPayload
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity

class SpawnPointProcessor: Processor() {
    val spawnCache = mutableMapOf<ServerPlayerEntity, ServerPlayerEntity.Respawn>()
    override fun makePacket(server: MinecraftServer, player: ServerPlayerEntity): CustomPayload? {
        val respawnData = player.respawn ?: return null
        val cached = spawnCache[player]
        if (cached?.pos != respawnData.pos || cached.dimension != respawnData.dimension) {
            spawnCache[player] = respawnData

            return SpawnPointPayload(SpawnPoint(respawnData.pos, respawnData.dimension))
        }

        return null
    }
}