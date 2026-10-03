package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusicapi.identifier.AdvancementIdentifier
import liltojustice.trueadaptivemusicapi.trigger.arguments.TriggerArguments
import liltojustice.trueadaptivemusicapi.trigger.predicate.type.StaticPredicateType
import net.minecraft.client.Minecraft
import kotlin.reflect.typeOf

object HasAdvancementPredicate: StaticPredicateType<HasAdvancementPredicate.Arguments>(
    "has_advancement", typeOf<Arguments>()
) {
    override val argDescriptions: Map<String, String>
        get() = super.argDescriptions + mapOf(
            Arguments::advancements.name to "List of advancements for music to play based on the qualifier.",
            Arguments::qualifier.name to "Whether music should play if the player has any, all, or none of the " +
                    "listed advancements."
        )
    override val tickRate: Int
        get() = super.tickRate * 20

    override fun test(arguments: Arguments): Boolean {
        val minecraft = Minecraft.getInstance()
        val playerEntity = minecraft.player ?: return false
        val predicate = { advancement: AdvancementIdentifier ->
            playerEntity.connection.advancements.get(advancement.id) != null
        }

        return when(arguments.qualifier) {
            Qualifier.Any -> arguments.advancements.any(predicate)
            Qualifier.All -> arguments.advancements.all(predicate)
            Qualifier.None -> arguments.advancements.none(predicate)
        }
    }

    data class Arguments(val advancements: List<AdvancementIdentifier>, val qualifier: Qualifier): TriggerArguments()

    enum class Qualifier {
        Any,
        All,
        None
    }
}