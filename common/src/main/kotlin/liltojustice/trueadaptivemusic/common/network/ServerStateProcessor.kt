package liltojustice.trueadaptivemusic.common.network

import liltojustice.trueadaptivemusic.common.network.processor.SpawnPointProcessor
import liltojustice.trueadaptivemusic.common.network.processor.StructureProcessor
import liltojustice.trueadaptivemusic.network.NetworkingCommon
import net.minecraft.server.MinecraftServer

object ServerStateProcessor {
    private val processors = listOf(
        StructureProcessor(),
        SpawnPointProcessor()
    )

    fun processServer(server: MinecraftServer) {
        server.playerList.players.forEach { player ->
            processors.forEach { processor ->
                processor.process(server, player)?.let { payload -> NetworkingCommon.sendToClient(player, payload) }
            }
        }
    }
}