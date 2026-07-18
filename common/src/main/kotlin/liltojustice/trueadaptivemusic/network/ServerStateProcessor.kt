package liltojustice.trueadaptivemusic.network

import liltojustice.trueadaptivemusic.network.model.CustomPacketPayload
import liltojustice.trueadaptivemusic.network.processor.Processor
import liltojustice.trueadaptivemusic.network.processor.SpawnPointProcessor
import liltojustice.trueadaptivemusic.network.processor.StructureProcessor
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer

object ServerStateProcessor {
    private val structureProcessor = StructureProcessor()
    private val spawnPointProcessor = SpawnPointProcessor()

    fun processServer(server: MinecraftServer, serverNetworkInterface: ServerNetworkInterface) {
        server.playerList.players.forEach { player ->
            processForClient(server, serverNetworkInterface, player, structureProcessor)
            processForClient(server, serverNetworkInterface, player, spawnPointProcessor)
        }
    }

    private fun <TPayload: CustomPacketPayload> processForClient(
        server: MinecraftServer,
        networkInterface: ServerNetworkInterface,
        player: ServerPlayer,
        processor: Processor<TPayload>
    ) {
        processor.process(server, player)?.let { result ->
            networkInterface.sendToClient(player, result.type, result.payload)
        }
    }
}