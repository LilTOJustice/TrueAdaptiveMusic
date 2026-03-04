package liltojustice.trueadaptivemusic.client.gui.widget.utility

import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.text.Text
import net.minecraft.text.TextColor
import net.minecraft.util.Colors

open class ClickableTextDisplayWidget(
    text: String,
    x: Int = 0,
    y: Int = 0,
    private val onClick: (ClickableTextDisplayWidget) -> Unit = {})
    : ClickableWidget(x, y, 0, 0, Text.literal(text)) {
    private val textRenderer = MinecraftClient.getInstance().textRenderer
    var color: Int = Colors.WHITE
    val text: String
        get() = message.string
    val coloredText: Text?
        get() = message.getWithStyle(message.style.withColor(TextColor.fromRgb(color))).firstOrNull()

    init {
        width = textRenderer.getWidth(message)
        height = textRenderer.fontHeight
    }

    override fun renderWidget(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        if (!visible) {
            return
        }

        context?.drawBorder(
            x + TEXT_OFFSET - BORDER_BUFFER / 2,
            y - BORDER_BUFFER / 2,
            width - TEXT_OFFSET / 2 + BORDER_BUFFER,
            height + BORDER_BUFFER,
            Colors.WHITE
        )

        x += TEXT_OFFSET
        drawScrollableText(
            context, textRenderer, coloredText, x, y, x + width, y + height, color)
        x -= TEXT_OFFSET
    }

    override fun onClick(mouseX: Double, mouseY: Double) {
        super.onClick(mouseX, mouseY)

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
        const val TEXT_OFFSET = 2
    }
}