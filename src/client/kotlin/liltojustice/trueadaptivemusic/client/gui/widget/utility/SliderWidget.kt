package liltojustice.trueadaptivemusic.client.gui.widget.utility

import net.minecraft.client.gui.widget.SliderWidget
import net.minecraft.text.Text
import kotlin.math.roundToInt

class SliderWidget(
    private val minimum: Int,
    private val maximum: Int,
    startingValue: Int = 0,
    private val title: String = "",
    private val onChange: (newValue: Int) -> Unit = {}
) : SliderWidget(
    0, 0, 150, 10, Text.literal(title), startingValue.toDouble() / maximum) {
    val actualValue: Int
        get() = (minimum + value * maximum).roundToInt()

    init {
        updateMessage()
    }

    override fun updateMessage() {
        message = Text.literal("$title: $actualValue")
    }

    override fun applyValue() {
        onChange(actualValue)
    }
}