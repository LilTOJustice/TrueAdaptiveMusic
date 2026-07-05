package liltojustice.trueadaptivemusic.common.client.trigger.predicate.types

import liltojustice.trueadaptivemusicapi.identifier.EntityIdentifier
import liltojustice.trueadaptivemusicapi.trigger.arguments.TriggerArguments
import liltojustice.trueadaptivemusicapi.trigger.predicate.type.StaticPredicateType
import net.minecraft.client.Minecraft
import kotlin.reflect.typeOf

object RidingPredicate: StaticPredicateType<RidingPredicate.Arguments>(
    "riding", typeOf<Arguments>()
) {
    override val argDescriptions: Map<String, String>
        get() = super.argDescriptions + mapOf(
            Arguments::entities.name to "Which entities to ride for the music to play. If none, any entity will " +
                    "trigger the music."
        )

    data class Arguments(val entities: List<EntityIdentifier>): TriggerArguments()

    override fun test(arguments: Arguments): Boolean {
        val vehicle = Minecraft.getInstance().player?.vehicle ?: return false

        return arguments.entities.isEmpty() || arguments.entities.any { entity -> entity.matches(vehicle) }
    }
}