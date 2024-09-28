package liltojustice.trueadaptivemusic.client.predicate.custompredicates

import com.google.gson.JsonObject
import kotlinx.serialization.json.Json
import liltojustice.trueadaptivemusic.client.predicate.MusicPredicate
import liltojustice.trueadaptivemusic.client.predicate.MusicPredicateException
import net.minecraft.client.MinecraftClient
import net.minecraft.registry.RegistryKeys
import net.minecraft.server.world.ServerWorld
import net.minecraft.structure.StructureStart
import net.minecraft.util.Identifier
import net.minecraft.util.JsonHelper
import net.minecraft.util.math.BlockBox
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkSectionPos
import net.minecraft.world.gen.structure.Structure
import java.nio.file.Path
import kotlin.math.max
import kotlin.math.min

class StructurePredicate internal constructor(partialPath: String, private val feature: Identifier) : MusicPredicate(partialPath) {
    private fun fullStructureTest(world: ServerWorld, x: Double, y: Double, z: Double): Boolean {
        val blockPos = BlockPos.ofFloored(x, y, z)
        val structureAccessor = world.structureAccessor
        val structure: Structure =
            structureAccessor.registryManager.get(RegistryKeys.STRUCTURE).get(feature) ?: return false

        var minX = Int.MAX_VALUE
        var minY = Int.MAX_VALUE
        var minZ = Int.MAX_VALUE
        var maxX = Int.MIN_VALUE
        var maxY = Int.MIN_VALUE
        var maxZ = Int.MIN_VALUE

        val structureStarts = structureAccessor.getStructureStarts(ChunkSectionPos.from(blockPos), structure)
        if (structureStarts.isEmpty())
        {
            return false
        }

        for (structureStart: StructureStart in structureStarts) {
            minX = min(minX, structureStart.boundingBox.minX)
            minY = min(minY, structureStart.boundingBox.minY)
            minZ = min(minZ, structureStart.boundingBox.minZ)
            maxX = max(maxX, structureStart.boundingBox.maxX)
            maxY = max(maxY, structureStart.boundingBox.maxY)
            maxZ = max(maxZ, structureStart.boundingBox.maxZ)
        }

        return BlockBox(minX, minY, minZ, maxX, maxY, maxZ).expand(20).contains(blockPos)
    }

    override fun test(client: MinecraftClient): Boolean {
        val serverWorld = client.server?.worlds?.firstOrNull { world ->
            world.registryKey == client.world?.registryKey } ?: return false
        val x: Double = client.player?.x ?: return false
        val y: Double = client.player?.y ?: return false
        val z: Double = client.player?.z ?: return false

        return serverWorld.canSetBlock(BlockPos.ofFloored(x, y, z)) && fullStructureTest(serverWorld, x, y, z)
    }

    override fun getIDs(): List<String> { return listOf(feature.toString()) }

    companion object: MusicPredicateCompanion<StructurePredicate> {
        override fun getTypeName(): String { return "structure" }

        override fun fromJson(json: JsonObject, partialPath: String): StructurePredicate {
            return StructurePredicate(partialPath, Identifier(JsonHelper.getString(json, "id")))
        }
    }
}