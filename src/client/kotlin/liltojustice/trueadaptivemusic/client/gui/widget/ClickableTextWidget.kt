package liltojustice.trueadaptivemusic.client.gui.widget

import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.text.Text
import net.minecraft.util.Colors

class ClickableTextWidget(
    private var text: String,
    x: Int = 0,
    y: Int = 0,
    private val showHighlight: Boolean = true,
    private val onClick: (ClickableTextWidget) -> Unit = {},
    private val isSelected: (ClickableTextWidget) -> Boolean = { false })
    : ClickableWidget(x, y, 0, 0, Text.literal("Clickable Text Widget")) {
    private val textRenderer = MinecraftClient.getInstance().textRenderer
    var color: Int = Colors.WHITE

    init {
        width = textRenderer.getWidth(text)
        height = textRenderer.fontHeight
    }

    override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        if (!visible) {
            return
        }

        val selected = isSelected(this)
        if (selected) {
            context?.drawBorder(
                x - BORDER_BUFFER / 2,
                y - BORDER_BUFFER / 2,
                width + BORDER_BUFFER,
                height + BORDER_BUFFER,
                Colors.WHITE)
        }

        if (!selected && showHighlight && isMouseOver(mouseX.toDouble(), mouseY.toDouble())) {
            context?.drawHorizontalLine(x, x + width, y + textRenderer.fontHeight, Colors.WHITE)
        }

        val textRenderer = MinecraftClient.getInstance().textRenderer
        context?.drawText(textRenderer, text, x, y, color, true)
        super.render(context, mouseX, mouseY, delta)
    }

    override fun onClick(mouseX: Double, mouseY: Double) {
        super.onClick(mouseX, mouseY)

        if (visible && active)
        {
            onClick(this)
        }
    }

    override fun renderButton(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
    }

    override fun appendClickableNarrations(builder: NarrationMessageBuilder?) {
    }

    fun setText(text: String) {
        this.text = text
        this.width = textRenderer.getWidth(text)
    }

    companion object {
        const val BORDER_BUFFER = 4
    }
}