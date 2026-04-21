package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusic.client.identifier.EntityTypeIdentifier
import liltojustice.trueadaptivemusicapi.trigger.TriggerArguments
import liltojustice.trueadaptivemusicapi.trigger.predicate.type.StaticPredicateType
import net.minecraft.client.Minecraft

class RidingPredicate: StaticPredicateType<RidingPredicate.Arguments>("riding") {
    override val argDescriptions: Map<String, String>
        get() = super.argDescriptions + mapOf(
            Arguments::entities.name to "Which entities to ride for the music to play. If none, any entity will " +
                    "trigger the music."
        )

    data class Arguments(val entities: List<EntityTypeIdentifier>): TriggerArguments()

    override fun validate(arguments: Arguments): Boolean {
        val vehicle = Minecraft.getInstance().player?.vehicle ?: return false

        return arguments.entities.isEmpty() || arguments.entities.any { entity -> entity.matches(vehicle) }
    }
}