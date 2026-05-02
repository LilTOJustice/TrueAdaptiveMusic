package liltojustice.trueadaptivemusic.network.model

import liltojustice.trueadaptivemusic.Constants.Companion.NULL_IDENTIFIER
import net.fabricmc.fabric.api.networking.v1.PlayerLookup
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.core.registries.Registries
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.levelgen.structure.Structure
import kotlin.collections.toList

object ServerStateProcessor {
    private val structureCache = mutableMapOf<Structure, Pair<Int, Int>>()

    fun processServer(server: MinecraftServer) {
        server.allLevels.forEach { level ->
            val structureManager = level.structureManager()
            val registryAccess = structureManager.registryAccess()
            val structureRegistry = registryAccess.lookup(Registries.STRUCTURE).get()
            val structureSetRegistry = registryAccess.lookup(Registries.STRUCTURE_SET).get()
            PlayerLookup.level(level).forEach { player ->
                val nearby = structureManager.getAllStructuresAt(player.blockPosition()).keys
                    .firstOrNull()?.takeIf { structure ->
                        val minMax = structureCache.getOrPut(structure) {
                            val starts = structureManager.startsForStructure(player.lastSectionPos, structure)

                            starts.maxOf { it.boundingBox.minY() } to starts.minOf { it.boundingBox.maxY() }
                        }

                        player.blockPosition().y.let { it >= minMax.first && it <= minMax.second }
                    }

                val structureId = nearby?.let { structureRegistry.getKey(it) } ?: NULL_IDENTIFIER
                val structureSetId = nearby?.let {
                    structureSetRegistry.getKey(
                        structureSetRegistry.toList().first { set ->
                            set.structures.any { structureSelection ->
                                structureId == structureSelection.structure().unwrapKey().get().identifier()
                            }
                        }
                    )
                } ?: NULL_IDENTIFIER

                ServerPlayNetworking.send(
                    player,
                    CurrentStructurePayload(structureId)
                )
                ServerPlayNetworking.send(
                    player,
                    CurrentStructureSetPayload(structureSetId)
                )
            }
        }
    }
}