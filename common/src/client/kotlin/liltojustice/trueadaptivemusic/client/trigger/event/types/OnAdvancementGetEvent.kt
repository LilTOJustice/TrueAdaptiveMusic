package liltojustice.trueadaptivemusic.client.trigger.event.types

import liltojustice.trueadaptivemusicapi.identifier.AdvancementIdentifier
import liltojustice.trueadaptivemusicapi.trigger.arguments.TriggerArguments
import liltojustice.trueadaptivemusicapi.trigger.event.input.EventInput
import liltojustice.trueadaptivemusicapi.trigger.event.type.StaticEventType
import net.minecraft.advancements.AdvancementHolder
import kotlin.reflect.typeOf


object OnAdvancementGetEvent
    : StaticEventType<OnAdvancementGetEvent.Arguments, OnAdvancementGetEvent.Input>(
    "on_advancement_get",
    typeOf<Arguments>()
) {
    override val argDescriptions: Map<String, String>
        get() = super.argDescriptions + mapOf(
            Arguments::advancements.name to "Which advancements music should play for. " +
                    "If none are selected, any advancement will trigger the music.")

    override fun validate(arguments: Arguments, input: Input): Boolean {
        return arguments.advancements.isEmpty() || arguments.advancements.any { it.matches(input.advancement) }
    }

    data class Arguments(val advancements: List<AdvancementIdentifier>): TriggerArguments()

    data class Input(val advancement: AdvancementHolder): EventInput()
}