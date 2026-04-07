package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusic.client.identifier.StructureSetIdentifier
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.Registries
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.levelgen.structure.StructureSet
import kotlin.collections.any
import kotlin.jvm.optionals.getOrNull

class StructureSetPredicate internal constructor(
    private val structureSets: List<StructureSetIdentifier>): MusicPredicate() {

    override fun test(): Boolean {
        val minecraft = Minecraft.getInstance()
        val dimensionKey = minecraft.level?.dimension() ?: return false
        val serverLevel = minecraft.singleplayerServer?.getLevel(dimensionKey) ?: return false
        val x: Double = minecraft.player?.x ?: return false
        val y: Double = minecraft.player?.y ?: return false
        val z: Double = minecraft.player?.z ?: return false

        return fullStructureTest(serverLevel, x, y, z)
    }

    override fun getTickRate(): Int {
        return super.getTickRate() * 20
    }

    private fun fullStructureTest(level: ServerLevel, x: Double, y: Double, z: Double): Boolean {
        val blockPos = BlockPos.containing(x, y, z)
        val structureManager = level.structureManager()
        val structuresNearby = structureManager.getAllStructuresAt(blockPos).keys

        return (structureSets.takeIf { structureSets.isNotEmpty() }
            ?.map { structureSet -> structureSet.id }
            ?: StructureSetIdentifier.getRegistryIds())
            .any { structureSetId ->
                val structureSet: StructureSet =
                    structureManager.registryAccess()
                        .lookup(Registries.STRUCTURE_SET).getOrNull()?.getValue(structureSetId)
                        ?: return false

                structureSet.structures.any { structureSelectionEntry ->
                    structuresNearby.any { structure ->
                        structureSelectionEntry.structure.value().type() == structure.type()
                    }
                }
            }
    }

    companion object: MusicPredicateCompanion {
        override val argDescriptions: Map<String, String>
            get() = super.argDescriptions + mapOf(
                StructureSetPredicate::structureSets.name to "Which structure sets the player must be in for the " +
                        "music should play. If none, any structure set will trigger the music."
            )
    }
}