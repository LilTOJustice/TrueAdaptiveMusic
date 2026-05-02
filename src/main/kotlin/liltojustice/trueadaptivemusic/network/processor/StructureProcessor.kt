package liltojustice.trueadaptivemusic.network.processor

import liltojustice.trueadaptivemusic.Constants
import liltojustice.trueadaptivemusic.network.model.CurrentStructurePayload
import net.minecraft.core.registries.Registries
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.levelgen.structure.Structure

class StructureProcessor: Processor {
    private val structureCache = mutableMapOf<Structure, Pair<Int, Int>>()
    override fun process(server: MinecraftServer, player: ServerPlayer): CustomPacketPayload {
        val level = player.level()
        val structureManager = level.structureManager()
        val registryAccess = structureManager.registryAccess()
        val structureRegistry = registryAccess.lookup(Registries.STRUCTURE).get()
        val structureSetRegistry = registryAccess.lookup(Registries.STRUCTURE_SET).get()
        val nearby = structureManager.getAllStructuresAt(player.blockPosition()).keys
            .firstOrNull()?.takeIf { structure ->
                val minMax = structureCache.getOrPut(structure) {
                    val starts = structureManager.startsForStructure(player.lastSectionPos, structure)

                    starts.maxOf { it.boundingBox.minY() } to starts.minOf { it.boundingBox.maxY() }
                }

                player.blockPosition().y.let { it >= minMax.first && it <= minMax.second }
            }

        val structureId = nearby?.let { structureRegistry.getKey(it) } ?: Constants.NULL_IDENTIFIER
        val structureSetId = nearby?.let {
            structureSetRegistry.getKey(
                structureSetRegistry.toList().first { set ->
                    set.structures.any { structureSelection ->
                        structureId == structureSelection.structure().unwrapKey().get().identifier()
                    }
                }
            )
        } ?: Constants.NULL_IDENTIFIER

        return CurrentStructurePayload(structureId, structureSetId)
    }
}