package liltojustice.trueadaptivemusic.client.gui.widget.utility

import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.client.sound.SoundManager
import net.minecraft.text.Text
import kotlin.math.min

class TextInputWidget(
    prompt: String,
    onChange: (widget: TextInputWidget, text: String) -> String,
    placeholder: String = "",
    x: Int = 0,
    y: Int = 0)
    : ClickableWidget(x, y, Int.MAX_VALUE, HEIGHT, Text.literal(prompt)) {
    private val textRenderer = MinecraftClient.getInstance().textRenderer
    private val promptWidget = run {
        val widget = ClickableTextWidget(prompt)
        widget.disableBold()

        widget
    }
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

    override fun playDownSound(soundManager: SoundManager?) {
    }

    override fun charTyped(chr: Char, modifiers: Int): Boolean {
        return fieldWidget.charTyped(chr, modifiers)
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        return fieldWidget.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun keyReleased(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        return fieldWidget.keyReleased(keyCode, scanCode, modifiers)
    }

    override fun renderWidget(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        if (fieldWidget.isFocused != isFocused) {
            fieldWidget.isFocused = isFocused
        }

        if (updateText.isNotEmpty()) {
            text = updateText
            updateText = ""
        }

        promptWidget.x = x
        promptWidget.y = y
        fieldWidget.y = y
        promptWidget.width = min(
            textRenderer.getWidth(promptWidget.text), width - fieldWidget.width - PADDING)
        fieldWidget.x = promptWidget.x + promptWidget.width + PADDING
        fieldWidget.width = textRenderer.getWidth(text) + 30

        promptWidget.render(context, mouseX, mouseY, delta)
        fieldWidget.render(context, mouseX, mouseY, delta)
    }

    override fun appendClickableNarrations(builder: NarrationMessageBuilder?) {
    }

    companion object {
        private const val HEIGHT = 10
        private const val PADDING = 5
    }
}