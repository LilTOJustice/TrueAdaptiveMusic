package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusic.client.identifier.StructureIdentifier
import liltojustice.trueadaptivemusicapi.trigger.arguments.TriggerArguments
import liltojustice.trueadaptivemusicapi.trigger.predicate.type.StaticPredicateType
import net.minecraft.client.MinecraftClient
import net.minecraft.registry.RegistryKeys
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import kotlin.jvm.optionals.getOrNull
import kotlin.reflect.typeOf

object StructurePredicate: StaticPredicateType<StructurePredicate.Arguments>(
    "structure", typeOf<Arguments>()
) {
    override val argDescriptions: Map<String, String>
        get() = super.argDescriptions + mapOf(
            Arguments::structures.name to "Which structures the player must be in for the music to play. If none, " +
                    "any structure will trigger the music."
        )
    override val tickRate: Int
        get() = super.tickRate * 20

    data class Arguments(val structures: List<StructureIdentifier>): TriggerArguments()

    override fun test(arguments: Arguments): Boolean {
        val minecraft = MinecraftClient.getInstance()
        val player = minecraft.player ?: return false
        val dimensionKey = minecraft.world?.registryKey ?: return false
        val serverLevel = minecraft.server?.getWorld(dimensionKey) ?: return false

        return fullStructureTest(arguments.structures, serverLevel, player.x, player.y, player.z)
    }

    private fun fullStructureTest(
        structures: List<StructureIdentifier>, level: ServerWorld, x: Double, y: Double, z: Double): Boolean {
        val blockPos = BlockPos.ofFloored(x, y, z)
        val structureManager = level.structureAccessor
        val structuresNearby = structureManager.getStructureReferences(blockPos).keys

        return (structures.takeIf { structures.isNotEmpty() }?.map { structure -> structure.id }
            ?: StructureIdentifier.getRegistryIds())
            .map { structureId ->
                structureManager
                    .registryManager
                    .getOptional(RegistryKeys.STRUCTURE)
                    .getOrNull()
                    ?.get(structureId)
                    ?.type
            }
            .any { structureType -> structuresNearby.any { structure -> structure.type == structureType } }
    }
}