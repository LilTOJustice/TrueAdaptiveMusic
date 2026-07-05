package liltojustice.trueadaptivemusic.common.client.trigger.predicate.types

import liltojustice.trueadaptivemusic.common.client.TAMNetworkingClient
import liltojustice.trueadaptivemusicapi.identifier.StructurePieceIdentifier
import liltojustice.trueadaptivemusicapi.trigger.arguments.TriggerArguments
import liltojustice.trueadaptivemusicapi.trigger.predicate.type.StaticPredicateType
import net.minecraft.client.Minecraft
import kotlin.collections.any
import kotlin.reflect.typeOf

object StructurePiecePredicate: StaticPredicateType<StructurePiecePredicate.Arguments>(
    "structure_piece", typeOf<Arguments>()
) {
    override val argDescriptions: Map<String, String>
        get() = super.argDescriptions + mapOf(
            Arguments::structurePieces.name to "Which structure pieces the player must be in for the music should play. " +
                    "If none, any structure set will trigger the music."
        )
    override val tickRate: Int
        get() = super.tickRate * 20

    data class Arguments(val structurePieces: List<StructurePieceIdentifier>): TriggerArguments()

    override fun test(arguments: Arguments): Boolean {
        return Minecraft.getInstance().player != null &&
                (arguments.structurePieces.isEmpty() ||
                        arguments.structurePieces.any { it.id == TAMNetworkingClient.structurePieceId })
    }
}