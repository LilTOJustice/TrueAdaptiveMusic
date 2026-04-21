package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusic.client.identifier.DimensionIdentifier
import liltojustice.trueadaptivemusicapi.trigger.TriggerArguments
import liltojustice.trueadaptivemusicapi.trigger.predicate.type.StaticPredicateType
import net.minecraft.client.Minecraft

class DimensionPredicate: StaticPredicateType<DimensionPredicate.Arguments>("dimension") {
    override val argDescriptions: Map<String, String>
        get() = super.argDescriptions + mapOf(
            Arguments::dimensions.name to "Select all dimensions the music should play for. If none, any " +
                    "dimension will trigger the music."
        )

    override fun validate(arguments: Arguments): Boolean {
        val playerDimension = Minecraft.getInstance().player?.level()?.dimensionTypeRegistration() ?: return false

        return arguments.dimensions.isEmpty() ||
                arguments.dimensions.any { dimension -> playerDimension.`is`((dimension.id)) }
    }

    data class Arguments(val dimensions: List<DimensionIdentifier>): TriggerArguments()
}