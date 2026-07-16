package liltojustice.trueadaptivemusic.network.processor

import liltojustice.trueadaptivemusic.network.model.SpawnPoint
import liltojustice.trueadaptivemusic.network.model.SpawnPointPayload
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer

class SpawnPointProcessor: Processor() {
    val spawnCache = mutableMapOf<ServerPlayer, ServerPlayer.RespawnConfig>()
    override fun makePacket(server: MinecraftServer, player: ServerPlayer): CustomPacketPayload? {
        val respawnData = player.respawnConfig ?: return null
        val cached = spawnCache[player]
        if (cached?.pos != respawnData.pos || cached.dimension != respawnData.dimension) {
            spawnCache[player] = respawnData

            return SpawnPointPayload(SpawnPoint(respawnData.pos, respawnData.dimension))
        }

        return null
    }
}