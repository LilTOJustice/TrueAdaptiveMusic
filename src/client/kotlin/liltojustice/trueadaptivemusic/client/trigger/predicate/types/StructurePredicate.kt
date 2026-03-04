package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusic.client.identifier.StructureIdentifier
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.MinecraftClient
import net.minecraft.registry.RegistryKeys
import net.minecraft.server.world.ServerWorld
import net.minecraft.structure.StructureStart
import net.minecraft.util.math.BlockBox
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkSectionPos
import net.minecraft.world.gen.StructureAccessor
import net.minecraft.world.gen.structure.Structure
import kotlin.math.max
import kotlin.math.min

class StructurePredicate internal constructor(private val structures: List<StructureIdentifier>): MusicPredicate() {
    private fun fullStructureTest(world: ServerWorld, x: Double, y: Double, z: Double): Boolean {
        val blockPos = BlockPos.ofFloored(x, y, z)
        val structureAccessor = world.structureAccessor

        return (structures.takeIf { structures.isNotEmpty() }?.map { structure -> structure.id }
            ?: StructureIdentifier.getRegistryIds())
            .any { structureId ->
                val structure: Structure =
                    structureAccessor.registryManager.get(RegistryKeys.STRUCTURE).get(structureId) ?: return false

                testStructure(structureAccessor, structure, blockPos)
            }
    }

    override fun getTickRate(): Int {
        return super.getTickRate() * 2
    }

    override fun test(): Boolean {
        val client = MinecraftClient.getInstance()
        val serverWorld = client.server?.worlds?.firstOrNull { world ->
            world.registryKey == client.world?.registryKey } ?: return false
        val x: Double = client.player?.x ?: return false
        val y: Double = client.player?.y ?: return false
        val z: Double = client.player?.z ?: return false

        return serverWorld.canSetBlock(BlockPos.ofFloored(x, y, z)) && fullStructureTest(serverWorld, x, y, z)
    }

    companion object: MusicPredicateCompanion {
        override val argDescriptions: Map<String, String>
            get() = super.argDescriptions + mapOf(
                "structures" to "Which structures the player must be in for the music to play. If none, any " +
                        "structure will trigger the music."
            )

        fun testStructure(structureAccessor: StructureAccessor, structure: Structure, blockPos: BlockPos): Boolean {
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
    }
}