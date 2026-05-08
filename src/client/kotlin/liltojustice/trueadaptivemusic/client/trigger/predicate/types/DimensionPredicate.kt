package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusicapi.identifier.DimensionIdentifier
import liltojustice.trueadaptivemusicapi.trigger.arguments.TriggerArguments
import liltojustice.trueadaptivemusicapi.trigger.predicate.type.StaticPredicateType
import net.minecraft.client.MinecraftClient
import kotlin.reflect.typeOf

object DimensionPredicate: StaticPredicateType<DimensionPredicate.Arguments>(
    "dimension", typeOf<Arguments>()
) {
    override val argDescriptions: Map<String, String>
        get() = super.argDescriptions + mapOf(
            Arguments::dimensions.name to "Select all dimensions the music should play for. If none, any " +
                    "dimension will trigger the music."
        )

    override fun test(arguments: Arguments): Boolean {
        val playerDimension = MinecraftClient.getInstance().world?.dimensionEntry ?: return false

        return arguments.dimensions.isEmpty() ||
                arguments.dimensions.any { dimension -> playerDimension.matchesId(dimension.id) }
    }

    data class Arguments(val dimensions: List<DimensionIdentifier>): TriggerArguments()
}