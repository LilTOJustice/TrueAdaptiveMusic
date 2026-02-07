package liltojustice.trueadaptivemusic.client.gui.widget.utility

import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.Click
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.client.gui.widget.TextWidget
import net.minecraft.text.Text
import kotlin.math.min

class TextInputWidget(
    private val screen: Screen,
    prompt: String,
    onChange: (widget: TextInputWidget, text: String) -> String,
    placeholder: String = "",
    x: Int = 0,
    y: Int = 0)
    : ClickableWidget(x, y, Int.MAX_VALUE, HEIGHT, Text.literal(prompt)) {
    private val textRenderer = MinecraftClient.getInstance().textRenderer
    private val promptWidget = TextWidget(Text.literal(prompt), textRenderer)
    private val fieldWidget = TextFieldWidget(
        textRenderer, 0, 0, Int.MAX_VALUE, HEIGHT, Text.literal(placeholder))
    var text: String
        get() { return fieldWidget.text }
        set(value) { fieldWidget.text = value }
    var updateText: String = ""

    init {
        fieldWidget.setChangedListener { text -> updateText = onChange(this, text).ifEmpty { "" } }
        text = placeholder
    }

    override fun renderWidget(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        if (updateText.isNotEmpty()) {
            text = updateText
            updateText = ""
        }

        promptWidget.x = x
        promptWidget.y = y
        fieldWidget.x = promptWidget.x + promptWidget.width + PADDING
        fieldWidget.y = y
        fieldWidget.width = min(textRenderer.getWidth(text) + 30, width - (fieldWidget.x - x))

        promptWidget.render(context, mouseX, mouseY, delta)
        fieldWidget.render(context, mouseX, mouseY, delta)
    }

    override fun mouseClicked(click: Click, doubled: Boolean): Boolean {
        val clicked = fieldWidget.mouseClicked(click, doubled)
        if (clicked) {
            screen.focused = fieldWidget
        }

        return clicked
    }

    override fun appendClickableNarrations(builder: NarrationMessageBuilder?) {
    }

    companion object {
        private const val HEIGHT = 10
        private const val PADDING = 5
    }
}