package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusic.client.network.TAMClientNetworking
import liltojustice.trueadaptivemusicapi.identifier.StructureSetIdentifier
import liltojustice.trueadaptivemusicapi.trigger.arguments.TriggerArguments
import liltojustice.trueadaptivemusicapi.trigger.predicate.type.StaticPredicateType
import net.minecraft.client.Minecraft
import kotlin.collections.any
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
        return Minecraft.getInstance().player != null &&
                (arguments.structureSets.isEmpty() ||
                        arguments.structureSets.any { it.id == TAMClientNetworking.structureSetId })
    }
}