package liltojustice.trueadaptivemusic.client.gui.widget.utility

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.input.CharacterEvent
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent
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
    private val textRenderer = Minecraft.getInstance().gameRenderer
    private val promptWidget = run {
        val widget = ClickableTextWidget(prompt)
        widget.disableBold()

        widget
    }

    private val fieldWidget = TextFieldWidget(
        textRenderer, 0, 0, Int.MAX_VALUE, HEIGHT, Component.literal(placeholder))
    var text: String
        get() { return fieldWidget.text }
        set(value) { fieldWidget.text = value }
    var updateText: String = ""

    init {
        fieldWidget.setChangedListener { text -> updateText = onChange(this, text).ifEmpty { "" } }
        text = placeholder
    }

    override fun playDownSound(soundManager: SoundManager) {
    }

    override fun charTyped(event: CharacterEvent): Boolean {
        return fieldWidget.charTyped(input)
    }

    override fun keyPressed(event: KeyEvent): Boolean {
        return fieldWidget.keyPressed(input)
    }

    override fun keyReleased(event: KeyEvent): Boolean {
        return fieldWidget.keyReleased(input)
    }

    override fun mouseClicked(event: MouseButtonEvent, doubled: Boolean): Boolean {
        return fieldWidget.mouseClicked(click, doubled)
    }

    override fun extractWidgetRenderState(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, delta: Float) {
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

        promptWidget.render(graphics, mouseX, mouseY, delta)
        fieldWidget.render(graphics, mouseX, mouseY, delta)
    }

    override fun updateWidgetNarration(output: NarrationElementOutput) {
    }

    companion object {
        private const val HEIGHT = 10
        private const val PADDING = 5
    }
}