package liltojustice.trueadaptivemusic.network.processor

import liltojustice.trueadaptivemusic.Constants
import liltojustice.trueadaptivemusic.network.model.CurrentStructurePayload
import net.fabricmc.fabric.api.networking.v1.FabricPacket
import net.minecraft.registry.RegistryKeys
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.world.gen.structure.Structure

class StructureProcessor: Processor() {
    private val structureCache = mutableMapOf<Structure, Pair<Int, Int>>()
    override fun makePacket(server: MinecraftServer, player: ServerPlayer): CustomPacketPayload {
        val level = player.level()
        val structureManager = level.structureManager()
        val registryAccess = structureManager.registryAccess()
        val structureRegistry = registryAccess.lookup(Registries.STRUCTURE).get()
        val structureSetRegistry = registryAccess.lookup(Registries.STRUCTURE_SET).get()
        val nearby = structureManager.getAllStructuresAt(player.blockPosition()).keys
            .firstOrNull { structure ->
                val minMax = structureCache.getOrPut(structure) {
                    val starts = structureManager.getStructureStarts(player.watchedSection, structure)

                    starts.maxOf { it.boundingBox.minY } to starts.minOf { it.boundingBox.maxY }
                }

                player.blockPos.y.let { it >= minMax.first && it <= minMax.second }
            }

        val structureId = nearby?.let { structureRegistry.getId(it) } ?: Constants.NULL_IDENTIFIER
        val structureSetId = nearby?.let {
            structureSetRegistry.getId(
                structureSetRegistry.toList().first { set ->
                    set.structures.any { structureSelection ->
                        structureId == structureSelection.structure().key.get().value
                    }
                }
            )
        } ?: Constants.NULL_IDENTIFIER

        return CurrentStructurePayload(structureId, structureSetId)
    }
}