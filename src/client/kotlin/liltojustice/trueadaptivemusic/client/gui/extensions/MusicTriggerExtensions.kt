package liltojustice.trueadaptivemusic.client.gui.extensions

import liltojustice.trueadaptivemusic.client.trigger.MusicTrigger
import net.minecraft.text.Text


fun MusicTrigger.getTriggerTooltipString(): String {
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
