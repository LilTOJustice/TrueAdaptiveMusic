package liltojustice.trueadaptivemusic.client.gui.extensions

import liltojustice.trueadaptivemusic.client.trigger.MusicTrigger
import liltojustice.trueadaptivemusic.client.trigger.event.ErrorEvent
import liltojustice.trueadaptivemusic.client.trigger.event.MusicEvent
import liltojustice.trueadaptivemusic.client.trigger.predicate.ErrorPredicate
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.text.MutableText
import net.minecraft.text.Text

fun MusicTrigger.getTriggerTooltipString(): String {
    return getTriggerTooltipText().string
}

fun MusicTrigger.getTriggerTooltipText(): MutableText {
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
    val args = getTriggerArgs()
    if (this is MusicPredicate) {
        result.appendLine("Predicate Arguments:")
    }
    else if (this is MusicEvent) {
        result.appendLine("Predicate Arguments:")
    }

    args.forEach { param -> result.appendLine(param.toString()) }

    if (args.isEmpty()) {
        result.append(
            Text.translatableWithFallback(
                "trueadaptivemusic.trigger_no_parameters", "No Arguments").string)
    }

    return Text.literal(result.trim().toString())
}
