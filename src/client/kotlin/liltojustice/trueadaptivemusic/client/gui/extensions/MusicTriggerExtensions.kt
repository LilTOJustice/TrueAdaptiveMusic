package liltojustice.trueadaptivemusic.client.gui.extensions

import liltojustice.trueadaptivemusic.client.trigger.MusicTrigger

import liltojustice.trueadaptivemusic.client.trigger.event.ErrorEvent
import liltojustice.trueadaptivemusic.client.trigger.event.MusicEvent
import liltojustice.trueadaptivemusic.client.trigger.predicate.ErrorPredicate
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import kotlin.reflect.full.memberProperties

fun MusicTrigger<*>.getTriggerId(): String {
    val args = arguments::class.memberProperties
    return type.typeName + if (args.isEmpty()) "" else "{${args.joinToString(",")}}"
}

fun MusicTrigger<*>.getTriggerTooltipText(): MutableComponent {
    if (arguments is ErrorPredicate.Arguments && state is ErrorPredicate.State) {
        return Component.literal(Component.translatableWithFallback(
            "trueadaptivemusic.trigger_error_predicate_tooltip",
            "Failed to load this predicate, so it will always be false.").string +
                "\n\n${
                    Component.translatableWithFallback("trueadaptivemusic.reason", "Reason").string}" +
                ": ${arguments.reason}\n\nJson: ${state.shortenedJson}")
    }

    if (arguments is ErrorEvent.Arguments && state is ErrorEvent.State) {
        return Component.literal(
            Component.translatableWithFallback(
                "trueadaptivemusic.trigger_error_event_tooltip",
                "Failed to load this event, so it will never trigger."
            ).string +
                    "\n\n${
                        Component.translatableWithFallback(
                            "trueadaptivemusic.reason", "Reason")
                    }: ${arguments.reason}\n\nJson: ${state.shortenedJson}"
        )
    }

    val result = StringBuilder()
    val args = MusicTrigger.getTriggerArgs(arguments)
    if (this is MusicPredicate<*>) {
        result.appendLine("Predicate Arguments:")
    }
    else if (this is MusicEvent<*>) {
        result.appendLine("Event Arguments:")
    }

    args.forEach { param -> result.appendLine(param.toString()) }

    if (args.isEmpty()) {
        result.append(
            Component.translatableWithFallback(
                "trueadaptivemusic.trigger_no_parameters", "No Arguments").string)
    }

    return Component.literal(result.trim().toString())
}
