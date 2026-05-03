package liltojustice.trueadaptivemusic.network

import liltojustice.trueadaptivemusic.network.processor.SpawnPointProcessor
import liltojustice.trueadaptivemusic.network.processor.StructureProcessor
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.server.MinecraftServer

class ServerStateProcessor {
    private val processors = listOf(
        StructureProcessor(),
        SpawnPointProcessor()
    )

    fun processServer(server: MinecraftServer) {
        server.playerManager.playerList.forEach { player ->
            processors.forEach { processor ->
                processor.process(server, player)?.let { payload -> ServerPlayNetworking.send(player, payload) }
            }
        }
    }
}