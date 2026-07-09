package liltojustice.trueadaptivemusic.common.client.gui.widget.utility

import net.minecraft.client.gui.components.AbstractSliderButton
import net.minecraft.network.chat.Component
import kotlin.math.roundToInt

class SliderWidget(
    private val minimum: Int,
    private val maximum: Int,
    startingValue: Int = 0,
    private val title: String = "",
    private val onChange: (newValue: Int) -> Unit = {}
): AbstractSliderButton(
    0,
    0,
    150,
    10,
    Component.literal(title),
    startingValue.toDouble() / maximum
) {
    val actualValue: Int
        get() = (minimum + value * maximum).roundToInt()

    init {
        updateMessage()
    }

    override fun updateMessage() {
        message = Component.literal("$title: $actualValue")
    }

    override fun applyValue() {
        onChange(actualValue)
    }
}