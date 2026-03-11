package liltojustice.trueadaptivemusic.client.gui.widget.utility

import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.text.Text

typealias WidgetMaker = (
    prompt: String,
    screen: Screen,
    outArgs: MutableList<Any?>,
    arg: InputWidgetMaker.WidgetArg,
    tooltipText: Text?,
    onChange: () -> Unit) -> ClickableWidget
