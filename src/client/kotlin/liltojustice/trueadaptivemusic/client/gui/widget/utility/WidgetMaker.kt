package liltojustice.trueadaptivemusic.client.gui.widget.utility

import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component

typealias WidgetMaker = (
    prompt: String,
    screen: Screen,
    outArgs: MutableList<Any?>,
    arg: InputWidgetMaker.WidgetArg,
    tooltipText: Component?,
    onChange: () -> Unit) -> AbstractWidget
