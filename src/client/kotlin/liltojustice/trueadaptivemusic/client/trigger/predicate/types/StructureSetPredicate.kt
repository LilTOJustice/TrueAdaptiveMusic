package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusic.client.identifier.StructureSetIdentifier
import liltojustice.trueadaptivemusicapi.trigger.arguments.TriggerArguments
import liltojustice.trueadaptivemusicapi.trigger.predicate.type.StaticPredicateType
import net.minecraft.client.MinecraftClient
import net.minecraft.registry.RegistryKeys
import net.minecraft.server.world.ServerWorld
import net.minecraft.structure.StructureSet
import net.minecraft.util.math.BlockPos
import kotlin.collections.any
import kotlin.jvm.optionals.getOrNull
import kotlin.reflect.typeOf

object StructureSetPredicate: StaticPredicateType<StructureSetPredicate.Arguments>(
    "structure_set", typeOf<Arguments>()
) {
    override val argDescriptions: Map<String, String>
        get() = super.argDescriptions + mapOf(
            Arguments::structureSets.name to "Which structure sets the player must be in for the music should play. " +
                    "If none, any structure set will trigger the music."
        )
    override val tickRate: Int
        get() = super.tickRate * 20

    data class Arguments(val structureSets: List<StructureSetIdentifier>): TriggerArguments()

    override fun test(arguments: Arguments): Boolean {
        val minecraft = MinecraftClient.getInstance()
        val dimensionKey = minecraft.world?.registryKey ?: return false
        val serverLevel = minecraft.server?.getWorld(dimensionKey) ?: return false
        val x: Double = minecraft.player?.x ?: return false
        val y: Double = minecraft.player?.y ?: return false
        val z: Double = minecraft.player?.z ?: return false

        return fullStructureTest(arguments.structureSets, serverLevel, x, y, z)
    }

    private fun fullStructureTest(
        structureSets: List<StructureSetIdentifier>, level: ServerWorld, x: Double, y: Double, z: Double): Boolean {
        val blockPos = BlockPos.ofFloored(x, y, z)
        val structureManager = level.structureAccessor
        val structuresNearby = structureManager.getStructureReferences(blockPos).keys

        return (structureSets.takeIf { structureSets.isNotEmpty() }
            ?.map { structureSet -> structureSet.id }
            ?: StructureSetIdentifier.getRegistryIds())
            .any { structureSetId ->
                val structureSet: StructureSet =
                    structureManager.registryManager
                        .getOptional(RegistryKeys.STRUCTURE_SET).getOrNull()?.get(structureSetId)
                        ?: return false

                structureSet.structures.any { structureSelectionEntry ->
                    structuresNearby.any { structure ->
                        structureSelectionEntry.structure.value().type == structure.type
                    }
                }
            }
    }
}