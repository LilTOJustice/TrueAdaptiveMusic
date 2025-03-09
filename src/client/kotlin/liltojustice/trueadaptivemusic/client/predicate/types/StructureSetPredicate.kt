package liltojustice.trueadaptivemusic.client.predicate.types

import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import liltojustice.trueadaptivemusic.client.identifier.StructureSetIdentifier
import net.minecraft.client.MinecraftClient
import net.minecraft.registry.RegistryKeys
import net.minecraft.server.world.ServerWorld
import net.minecraft.structure.StructureSet
import net.minecraft.util.JsonHelper
import net.minecraft.util.math.BlockPos

class StructureSetPredicate internal constructor(private val structureSet: StructureSetIdentifier): MusicPredicate() {
    private fun fullStructureTest(world: ServerWorld, x: Double, y: Double, z: Double): Boolean {
        val blockPos = BlockPos.ofFloored(x, y, z)
        val structureAccessor = world.structureAccessor
        val structureSet: StructureSet =
            structureAccessor.registryManager.get(RegistryKeys.STRUCTURE_SET).get(structureSet) ?: return false

        return structureSet.structures.any { structureWeightedEntry ->
            StructurePredicate.testStructure(structureAccessor, structureWeightedEntry.structure.value(), blockPos) }
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
        result.add("id", JsonPrimitive(structureSet.toString()))

        return result
    }

    companion object: MusicPredicateCompanion<StructureSetPredicate> {
        override fun getTypeName(): String { return "structure_set" }

        override fun fromJson(json: JsonObject): StructureSetPredicate {
            return StructureSetPredicate(StructureSetIdentifier(JsonHelper.getString(json, "id")))
        }
    }
}