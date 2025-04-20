package liltojustice.trueadaptivemusic.client.gui.extensions

import liltojustice.trueadaptivemusic.client.trigger.MusicTrigger
import liltojustice.trueadaptivemusic.client.trigger.event.ErrorEvent
import liltojustice.trueadaptivemusic.client.trigger.predicate.ErrorPredicate
import net.minecraft.text.Text

fun MusicTrigger.getTriggerTooltipString(): String {
    if (this is ErrorPredicate) {
        return "Failed to load this predicate, so it will always be false.\n\nReason: $reason\n\nJson: $shortenedJson"
    }

    if (this is ErrorEvent) {
        return "Failed to load this event, so it will never trigger.\n\nReason: $reason\n\nJson: $shortenedJson"
    }

    val result = StringBuilder()
    val params = getTriggerParams()
    params.forEach { param -> result.appendLine(param.toString()) }

    if (params.isEmpty()) {
        result.append("No parameters")
    }

    return result.trim().toString()
}

fun MusicTrigger.getTriggerTooltipText(): Text {
    return Text.literal(getTriggerTooltipString())
}
