package liltojustice.trueadaptivemusic.client.gui.extensions

import liltojustice.trueadaptivemusic.client.trigger.MusicTrigger
import liltojustice.trueadaptivemusic.client.trigger.event.ErrorEvent
import liltojustice.trueadaptivemusic.client.trigger.predicate.ErrorPredicate
import net.minecraft.text.Text

fun MusicTrigger.getTriggerTooltipString(): String {
    return getTriggerTooltipText().string
}

fun MusicTrigger.getTriggerTooltipText(): Text {
    if (this is ErrorPredicate) {
        return Text.literal(Text.translatableWithFallback(
            "trueadaptivemusic.trigger_error_predicate_tooltip",
            "Failed to load this predicate, so it will always be false.").string +
                    "\n\n${Text.translatableWithFallback("trueadaptivemusic.reason", "Reason").string}" +
                ": $reason\n\nJson: $shortenedJson")
    }

    if (this is ErrorEvent) {
        return Text.literal(Text.translatableWithFallback(
            "trueadaptivemusic.trigger_error_predicate_tooltip",
            "Failed to load this event, so it will never trigger.").string + "\n\n${Text.translatableWithFallback(
                "trueadaptivemusic.reason", "Reason")}: $reason\n\nJson: $shortenedJson")
    }

    val result = StringBuilder()
    val params = getTriggerParams()
    params.forEach { param -> result.appendLine(param.toString()) }

    if (params.isEmpty()) {
        result.append(
            Text.translatableWithFallback(
                "trueadaptivemusic.trigger_no_parameters", "No Parameters").string)
    }

    return Text.literal(result.trim().toString())
}
