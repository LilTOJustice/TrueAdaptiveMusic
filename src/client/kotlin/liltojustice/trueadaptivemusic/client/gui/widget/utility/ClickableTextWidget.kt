package liltojustice.trueadaptivemusic.client.gui.widget.utility

import liltojustice.trueadaptivemusic.client.gui.extensions.drawBorder
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.Click
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.text.Text
import net.minecraft.util.Colors

open class ClickableTextWidget(
    text: String,
    x: Int = 0,
    y: Int = 0,
    private val showHighlight: Boolean = true,
    private val onClick: (ClickableTextWidget) -> Unit = {},
    private val isSelected: (ClickableTextWidget) -> Boolean = { false })
    : ClickableWidget(x, y, 0, 0, Text.literal(text)),
    DataWrapped<ClickableTextWidget> {
    override var customData: Any? = null
    private val textRenderer = MinecraftClient.getInstance().textRenderer
    var color: Int = Colors.WHITE
    val text: String
        get() = message.string

    init {
        width = textRenderer.getWidth(message)
        height = textRenderer.fontHeight
    }

    override fun renderWidget(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        if (!visible) {
            return
        }

        val selected = isSelected(this)
        if (selected) {
            context?.drawBorder(x, y, width, height, padding = BORDER_BUFFER)
        }

        if (!selected && showHighlight && isMouseOver(mouseX.toDouble(), mouseY.toDouble())) {
            context?.drawHorizontalLine(x, x + width, y + textRenderer.fontHeight, Colors.WHITE)
        }

        drawScrollableText(context, textRenderer, message, x, y, x + width, y + height, color)
    }

    override fun onClick(click: Click, doubled: Boolean) {
        super.onClick(click, doubled)

        if (visible && active)
        {
            onClick(this)
        }
    }

    override fun appendClickableNarrations(builder: NarrationMessageBuilder?) {
    }

    fun setText(text: String) {
        message = Text.literal(text)
        this.width = textRenderer.getWidth(message)
    }

    companion object {
        const val BORDER_BUFFER = 4
    }
}