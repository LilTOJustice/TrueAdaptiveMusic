package liltojustice.trueadaptivemusic.client.gui.widget.utility

import liltojustice.trueadaptivemusic.Logger
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import kotlin.reflect.KParameter
import kotlin.reflect.KType

class InputWidgetMaker {
    private val widgetRegistry = ArrayDeque<WidgetRegistryEntry>()

    fun register(predicate: (parameterType: KType) -> Boolean, widgetMaker: WidgetMaker) {
        widgetRegistry.addFirst(WidgetRegistryEntry(predicate, widgetMaker))
    }

    fun makeWidget(
        screen: Screen,
        outArgs: MutableList<Any?>,
        arg: WidgetArg,
        displayName: Component?,
        tooltipText: Component?,
        onChange: () -> Unit
    ): AbstractWidget {
        val displayName = displayName?.string ?: arg.name ?: "Unknown"
        return widgetRegistry.firstOrNull { entry -> entry.predicate(arg.type) }
            ?.widgetMaker(displayName, screen, outArgs, arg, tooltipText, onChange)
            ?: run {
                Logger.logWarning("Couldn't create widget for expected type ${arg.type}.")
                EmptyClickableWidget()
            }
    }

    data class WidgetArg(val type: KType, val name: String?, val index: Int) {
        companion object {
            fun of(kParameter: KParameter): WidgetArg {
                return WidgetArg(kParameter.type, kParameter.name, kParameter.index)
            }
        }
    }

    private data class WidgetRegistryEntry(
        val predicate: (parameterType: KType) -> Boolean, val widgetMaker: WidgetMaker) {
    }
}