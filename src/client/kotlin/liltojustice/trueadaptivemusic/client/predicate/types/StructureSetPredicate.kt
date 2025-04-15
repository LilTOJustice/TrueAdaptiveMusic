package liltojustice.trueadaptivemusic.client.predicate.types

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.identifier.StructureSetIdentifier
import liltojustice.trueadaptivemusic.client.predicate.MusicPredicate
import net.minecraft.client.MinecraftClient
import net.minecraft.registry.RegistryKeys
import net.minecraft.server.world.ServerWorld
import net.minecraft.structure.StructureSet
import net.minecraft.util.JsonHelper
import net.minecraft.util.math.BlockPos

class StructureSetPredicate internal constructor(private val structureSets: List<StructureSetIdentifier>): MusicPredicate() {
    private fun fullStructureTest(world: ServerWorld, x: Double, y: Double, z: Double): Boolean {
        val blockPos = BlockPos.ofFloored(x, y, z)
        val structureAccessor = world.structureAccessor

        return (structureSets.takeIf { structureSets.isNotEmpty() } ?: StructureSetIdentifier.getRegistryIds())
            .any { structureSetId ->
                val structureSet: StructureSet =
                    structureAccessor.registryManager.get(RegistryKeys.STRUCTURE_SET).get(structureSetId) ?: return false

                structureSet.structures.any { structureWeightedEntry ->
                    StructurePredicate.testStructure(structureAccessor, structureWeightedEntry.structure.value(), blockPos) }
            }
    }

    override fun test(client: MinecraftClient): Boolean {
        val serverWorld = client.server?.worlds?.firstOrNull { world ->
            world.registryKey == client.world?.registryKey } ?: return false
        val x: Double = client.player?.x ?: return false
        val y: Double = client.player?.y ?: return false
        val z: Double = client.player?.z ?: return false

        return serverWorld.canSetBlock(BlockPos.ofFloored(x, y, z)) && fullStructureTest(serverWorld, x, y, z)
    }

    override fun toJson(): JsonObject {
        val result = super.toJson()
        val jsonStructureSets = JsonArray()
        structureSets.forEach { structureSet -> jsonStructureSets.add(structureSet.toString()) }
        result.add("id", jsonStructureSets)

        return result
    }

    companion object: MusicPredicateCompanion<StructureSetPredicate> {
        override fun getTypeName(): String { return "structure_set" }

        override fun fromJson(json: JsonObject): StructureSetPredicate {
            return StructureSetPredicate(
                if (JsonHelper.hasArray(json, "id"))
                    JsonHelper.getArray(json, "id").map { element -> StructureSetIdentifier(element.asString) }
                else
                    listOf(StructureSetIdentifier(JsonHelper.getString(json, "id"))))
        }
    }
}