package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusic.client.identifier.StructureIdentifier
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import kotlin.math.max
import kotlin.math.min

class StructurePredicate internal constructor(private val structures: List<StructureIdentifier>): MusicPredicate() {
    override fun test(): Boolean {
        val minecraft = Minecraft.getInstance()
        val serverWorld = minecraft.server?.worlds?.firstOrNull { world ->
            world.registryKey == minecraft.world?.registryKey } ?: return false
        val x: Double = minecraft.player?.x ?: return false
        val y: Double = minecraft.player?.y ?: return false
        val z: Double = minecraft.player?.z ?: return false

        return fullStructureTest(serverWorld, x, y, z)
    }

    override fun getTickRate(): Int {
        return super.getTickRate() * 20
    }

    private fun fullStructureTest(level: ServerLevel, x: Double, y: Double, z: Double): Boolean {
        val blockPos = BlockPos.containing(x, y, z)
        val structureAccessor = level.structureManager

        return (structures.takeIf { structures.isNotEmpty() }?.map { structure -> structure.id }
            ?: StructureIdentifier.getRegistryIds())
            .any { structureId ->
                val structure: Structure =
                    structureAccessor.registryManager
                        .getOptional(RegistryKeys.STRUCTURE).get().get(structureId) ?: return false

                testStructure(structureAccessor, structure, blockPos)
            }
    }

    companion object: MusicPredicateCompanion {
        override val argDescriptions: Map<String, String>
            get() = super.argDescriptions + mapOf(
                StructurePredicate::structures.name to "Which structures the player must be in for the music to " +
                        "play. If none, any structure will trigger the music."
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