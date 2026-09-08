package liltojustice.trueadaptivemusic.client.gui.widget.utility

import net.minecraft.client.Minecraft
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.input.CharacterEvent
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.sounds.SoundManager
import net.minecraft.network.chat.Component
import kotlin.math.min

class TextInputWidget(
    prompt: String,
    onChange: (widget: TextInputWidget, text: String) -> String,
    placeholder: String = "",
    x: Int = 0,
    y: Int = 0)
    : AbstractWidget(x, y, Int.MAX_VALUE, HEIGHT, Component.literal(prompt)) {
    private val textRenderer = Minecraft.getInstance().font
    private val promptWidget = run {
        val widget = ClickableTextWidget(prompt)
        widget.withoutBold()

        widget
    }
    private val fieldWidget = EditBox(
        font,
        0,
        0,
        Int.MAX_VALUE,
        HEIGHT,
        Component.literal(placeholder)
    ).also { it.setMaxLength(MAX_INPUT) }

    var text: String
        get() { return fieldWidget.value }
        set(value) {
            fieldWidget.setValue(value)
        }
    var updateText: String = ""

    init {
        fieldWidget.setResponder { text -> updateText = onChange(this, text).ifEmpty { "" } }
        text = placeholder
    }

    override fun playDownSound(soundManager: SoundManager) {
    }

    override fun charTyped(input: CharacterEvent): Boolean {
        return fieldWidget.charTyped(input)
    }

    override fun keyPressed(input: KeyEvent): Boolean {
        return fieldWidget.keyPressed(input)
    }

    override fun keyReleased(input: KeyEvent): Boolean {
        return fieldWidget.keyReleased(input)
    }

    override fun mouseClicked(click: MouseButtonEvent, doubled: Boolean): Boolean {
        return fieldWidget.mouseClicked(click, doubled)
    }

    override fun renderWidget(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
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
            textRenderer.width(promptWidget.text), width - fieldWidget.width - PADDING)
        fieldWidget.x = promptWidget.x + promptWidget.width + PADDING
        fieldWidget.width = textRenderer.width(text) + 30

        promptWidget.render(context, mouseX, mouseY, delta)
        fieldWidget.render(context, mouseX, mouseY, delta)
    }

    override fun updateWidgetNarration(builder: NarrationElementOutput) {
    }

    companion object {
        private const val HEIGHT = 10
        private const val PADDING = 5
        private const val MAX_INPUT = 1000
    }
}