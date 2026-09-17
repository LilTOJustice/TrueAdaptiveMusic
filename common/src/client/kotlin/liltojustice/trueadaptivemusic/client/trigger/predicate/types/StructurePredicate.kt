package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusic.Constants
import liltojustice.trueadaptivemusic.client.network.TAMClientNetworking
import liltojustice.trueadaptivemusicapi.identifier.StructureIdentifier
import liltojustice.trueadaptivemusicapi.trigger.arguments.TriggerArguments
import liltojustice.trueadaptivemusicapi.trigger.predicate.type.StaticPredicateType
import net.minecraft.client.Minecraft
import kotlin.reflect.typeOf

object StructurePredicate: StaticPredicateType<StructurePredicate.Arguments>(
    "structure", typeOf<Arguments>()
) {
    override val argDescriptions: Map<String, String>
        get() = super.argDescriptions + mapOf(
            Arguments::structures.name to "Which structures the player must be in for the music to play. If none, " +
                    "any structure will trigger the music."
        )

    data class Arguments(val structures: List<StructureIdentifier>): TriggerArguments()

    override fun test(arguments: Arguments): Boolean {
        return Minecraft.getInstance().player != null &&
                ((arguments.structures.isEmpty() && TAMClientNetworking.structureId != Constants.NULL_IDENTIFIER)
                        || arguments.structures.any { it.id == TAMClientNetworking.structureId })
    }
}