package liltojustice.trueadaptivemusic.common.network

import liltojustice.trueadaptivemusic.common.network.processor.SpawnPointProcessor
import liltojustice.trueadaptivemusic.common.network.processor.StructureProcessor
import net.minecraft.server.MinecraftServer

class ServerStateProcessor {
    private val processors = listOf(
        StructureProcessor(),
        SpawnPointProcessor()
    )

    fun processServer(server: MinecraftServer) {
    }
}