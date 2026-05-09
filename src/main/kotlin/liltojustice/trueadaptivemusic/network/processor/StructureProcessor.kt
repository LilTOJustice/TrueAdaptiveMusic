package liltojustice.trueadaptivemusic.network.processor

import liltojustice.trueadaptivemusic.Constants
import liltojustice.trueadaptivemusic.network.model.CurrentStructurePayload
import net.fabricmc.fabric.api.networking.v1.FabricPacket
import net.minecraft.registry.RegistryKeys
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.structure.StructurePiece
import net.minecraft.util.math.ChunkSectionPos
import net.minecraft.world.gen.structure.Structure

class StructureProcessor: Processor() {
    private val structureCache = mutableMapOf<ChunkSectionPos, StructureData>()
    override fun makePacket(server: MinecraftServer, player: ServerPlayerEntity): FabricPacket {
        val level = player.serverWorld
        val structureManager = level.structureAccessor
        val registryAccess = structureManager.registryManager
        val structureRegistry = registryAccess.getOptional(RegistryKeys.STRUCTURE).get()
        val structureSetRegistry = registryAccess.getOptional(RegistryKeys.STRUCTURE_SET).get()
        val structurePieceRegistry = registryAccess.getOptional(RegistryKeys.STRUCTURE_PIECE).get()
        val nearby = structureManager.getStructureReferences(player.blockPos).keys
            .map { structure ->
                val sectionPos = player.watchedSection
                structureCache.getOrPut(sectionPos) {
                    val starts = structureManager.getStructureStarts(sectionPos, structure)
                    val bounds = starts.takeIf { it.isNotEmpty() }?.let {
                        starts.maxOf { it.boundingBox.minY } to starts.minOf { it.boundingBox.maxY }
                    }

                    StructureData(structure, starts.flatMap { it.children }.toSet(), bounds)
                }
            }
            .firstOrNull { structureData ->
                structureData.yBounds?.let { yBounds ->
                    player.blockPos.y.let { it >= yBounds.first && it <= yBounds.second }
                } ?: false
            }

        val structurePieceId = nearby?.let {
            it.pieces.firstOrNull { piece -> piece.boundingBox.contains(player.blockPos) }
                ?.let { piece -> structurePieceRegistry.getId(piece.type) }
        } ?: Constants.NULL_IDENTIFIER
        val structureId = nearby?.let { structureRegistry.getId(it.structure) } ?: Constants.NULL_IDENTIFIER
        val structureSetId = nearby?.let {
            structureSetRegistry.getId(
                structureSetRegistry.toList().first { set ->
                    set.structures.any { structureSelection ->
                        structureId == structureSelection.structure().key.get().value
                    }
                }
            )
        } ?: Constants.NULL_IDENTIFIER

        return CurrentStructurePayload(
            structureId, structureSetId, structurePieceId)
    }

    data class StructureData(val structure: Structure, val pieces: Set<StructurePiece>, val yBounds: Pair<Int, Int>?)
}