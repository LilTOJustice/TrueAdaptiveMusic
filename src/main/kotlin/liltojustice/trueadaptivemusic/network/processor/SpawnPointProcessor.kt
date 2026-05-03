package liltojustice.trueadaptivemusic.network.processor

import liltojustice.trueadaptivemusic.network.model.SpawnPointPayload
import net.minecraft.network.packet.CustomPayload
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.world.WorldProperties

class SpawnPointProcessor: Processor() {
    val spawnCache = mutableMapOf<ServerPlayerEntity, WorldProperties.SpawnPoint>()
    override fun makePacket(server: MinecraftServer, player: ServerPlayerEntity): CustomPayload? {
        val respawnData = player.respawn?.respawnData() ?: return null
        val cached = spawnCache[player]
        if (cached?.globalPos != respawnData.globalPos) {
            spawnCache[player] = respawnData

            return SpawnPointPayload(respawnData)
        }

        return null
    }
}