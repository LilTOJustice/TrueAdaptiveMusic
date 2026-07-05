package liltojustice.trueadaptivemusic.common.network.processor

import liltojustice.trueadaptivemusic.common.network.model.SpawnPointPayload
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.storage.LevelData

class SpawnPointProcessor: Processor() {
    val spawnCache = mutableMapOf<ServerPlayer, LevelData.RespawnData>()
    override fun makePacket(server: MinecraftServer, player: ServerPlayer): CustomPacketPayload? {
        val respawnData = player.respawnConfig?.respawnData() ?: return null
        val cached = spawnCache[player]
        if (cached?.globalPos != respawnData.globalPos) {
            spawnCache[player] = respawnData

            return SpawnPointPayload(respawnData)
        }

        return null
    }
}