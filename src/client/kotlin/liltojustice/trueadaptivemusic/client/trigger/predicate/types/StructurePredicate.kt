package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusic.client.identifier.StructureIdentifier
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.Registries
import net.minecraft.server.level.ServerLevel
import kotlin.jvm.optionals.getOrNull

class StructurePredicate internal constructor(private val structures: List<StructureIdentifier>): MusicPredicate() {
    override fun test(): Boolean {
        val minecraft = Minecraft.getInstance()
        val player = minecraft.player ?: return false
        val dimensionKey = minecraft.level?.dimension() ?: return false
        val serverLevel = minecraft.singleplayerServer?.getLevel(dimensionKey) ?: return false

        return fullStructureTest(serverLevel, player.x, player.y, player.z)
    }

    override fun getTickRate(): Int {
        return super.getTickRate() * 20
    }

    private fun fullStructureTest(level: ServerLevel, x: Double, y: Double, z: Double): Boolean {
        val blockPos = BlockPos.containing(x, y, z)
        val structureManager = level.structureManager()
        val structuresNearby = structureManager.getAllStructuresAt(blockPos).keys

        return (structures.takeIf { structures.isNotEmpty() }?.map { structure -> structure.id }
            ?: StructureIdentifier.getRegistryIds())
            .map { structureId ->
                structureManager
                    .registryAccess()
                    .lookup(Registries.STRUCTURE)
                    .getOrNull()
                    ?.getValue(structureId)
                    ?.type()
            }
            .any { structureType -> structuresNearby.any { structure -> structure.type() == structureType } }
    }

    companion object: MusicPredicateCompanion {
        override val argDescriptions: Map<String, String>
            get() = super.argDescriptions + mapOf(
                StructurePredicate::structures.name to "Which structures the player must be in for the music to " +
                        "play. If none, any structure will trigger the music."
            )
    }
}