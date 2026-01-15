package liltojustice.trueadaptivemusic.client.gui.widget.utility

import liltojustice.trueadaptivemusic.Logger
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ClickableWidget
import kotlin.reflect.KParameter
import kotlin.reflect.KType

class InputWidgetMaker {
    private val widgetRegistry = ArrayDeque<WidgetRegistryEntry>()

    fun register(
        predicate: (parameterType: KType) -> Boolean,
        widgetMaker:
            (prompt: String,
             screen: Screen,
             outArgs: MutableList<Any?>,
             arg: KParameter,
             onChange: () -> Unit) -> ClickableWidget) {
        widgetRegistry.addFirst(WidgetRegistryEntry(predicate, widgetMaker))
    }

    fun makeWidget(screen: Screen, outArgs: MutableList<Any?>, arg: KParameter, onChange: () -> Unit): ClickableWidget {
        val prompt = (arg.name ?: "Unknown") +
                ": ${arg.type.toString().split('.').last().replace(">", "")}"
        return widgetRegistry.firstOrNull { entry -> entry.predicate(arg.type) }
            ?.widgetMaker(prompt, screen, outArgs, arg, onChange)
            ?: run {
                Logger.logWarning("Couldn't create widget for expected type ${arg.type}.")
                EmptyClickableWidget()
            }
    }

    private data class WidgetRegistryEntry(
        val predicate: (parameterType: KType) -> Boolean,
        val widgetMaker: (
            prompt: String,
            screen: Screen,
            outArgs: MutableList<Any?>,
            arg: KParameter,
            onChange: () -> Unit) -> ClickableWidget) {
    }
}