package liltojustice.trueadaptivemusic.network.processor

import liltojustice.trueadaptivemusic.Constants
import liltojustice.trueadaptivemusic.network.model.CurrentStructurePayload
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.core.registries.Registries
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.levelgen.structure.StructurePiece
import net.minecraft.core.SectionPos
import net.minecraft.world.level.levelgen.structure.Structure

class StructureProcessor: Processor() {
    private val structureCache = mutableMapOf<Pair<SectionPos, Structure>, StructureData>()
    override fun makePacket(server: MinecraftServer, player: ServerPlayer): CustomPacketPayload {
        val level = player.level()
        val structureManager = level.structureManager()
        val registryAccess = structureManager.registryAccess()
        val structureRegistry = registryAccess.lookup(Registries.STRUCTURE).get()
        val structureSetRegistry = registryAccess.lookup(Registries.STRUCTURE_SET).get()
        val structurePieceRegistry = registryAccess.lookup(Registries.STRUCTURE_PIECE).get()
        val nearby = structureManager.getAllStructuresAt(player.blockPosition()).keys
            .map { structure ->
                val sectionPos = player.lastSectionPos
                structureCache.getOrPut(sectionPos to structure) {
                    val starts = structureManager.startsForStructure(sectionPos, structure)
                    val bounds = starts.takeIf { it.isNotEmpty() }?.let {
                        starts.maxOf { it.boundingBox.minY() } to starts.minOf { it.boundingBox.maxY() }
                    }

                    StructureData(structure, starts.flatMap { it.pieces }.toSet(), bounds)
                }
            }
            .firstOrNull { structureData ->
                structureData.yBounds?.let { yBounds ->
                    player.blockPosition().y.let { it >= yBounds.first && it <= yBounds.second }
                } ?: false
            }

        val structurePieceId = nearby?.let {
            it.pieces.firstOrNull { piece -> piece.boundingBox.isInside(player.blockPosition()) }
                ?.let { piece -> structurePieceRegistry.getKey(piece.type) }
        } ?: Constants.NULL_IDENTIFIER
        val structureId = nearby?.let { structureRegistry.getKey(it.structure) } ?: Constants.NULL_IDENTIFIER
        val structureSetId = nearby?.let {
            structureSetRegistry.toList().firstOrNull { set ->
                set.structures.any { structureSelection ->
                    structureId == structureSelection.structure().unwrapKey().get().location()
                }
            }?.let { structureSetRegistry.getKey(it) }
        } ?: Constants.NULL_IDENTIFIER

        return CurrentStructurePayload(
            structureId, structureSetId, structurePieceId)
    }

    data class StructureData(val structure: Structure, val pieces: Set<StructurePiece>, val yBounds: Pair<Int, Int>?)
}