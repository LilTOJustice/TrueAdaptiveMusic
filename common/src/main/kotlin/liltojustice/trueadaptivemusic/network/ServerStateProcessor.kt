package liltojustice.trueadaptivemusic.network

import liltojustice.trueadaptivemusic.network.processor.SpawnPointProcessor
import liltojustice.trueadaptivemusic.network.processor.StructureProcessor
import net.minecraft.server.MinecraftServer

object ServerStateProcessor {
    private val processors = listOf(
        StructureProcessor(),
        SpawnPointProcessor()
    )

    fun processServer(server: MinecraftServer, serverNetworkInterface: ServerNetworkInterface) {
        server.playerList.players.forEach { player ->
            processors.forEach { processor ->
                processor.process(server, player)?.let { payload -> serverNetworkInterface.sendToClient(player, payload) }
            }
        }
    }
}