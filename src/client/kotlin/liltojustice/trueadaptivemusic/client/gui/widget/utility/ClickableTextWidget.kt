package liltojustice.trueadaptivemusic.client.gui.widget.utility

import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.text.Text
import net.minecraft.text.TextColor
import net.minecraft.util.Colors

open class ClickableTextWidget(
    text: String,
    x: Int = 0,
    y: Int = 0,
    private val showHighlight: Boolean = true,
    private val onClick: ((ClickableTextWidget) -> Unit)? = null,
    private val isSelected: (ClickableTextWidget) -> Boolean = { false },
    private val onMouseOn: (ClickableTextWidget) -> Unit = {},
    private val onMouseOff: (ClickableTextWidget) -> Unit = {}
): ClickableWidget(x, y, 0, 0, Text.literal(text)) {
    var color: Int = Colors.WHITE
    val text: String
        get() = message.string
    private val textRenderer = MinecraftClient.getInstance().textRenderer
    private var disableBold = false
    private var enableItalic = false
    private val styledText: Text
        get() = run {
            var style = message.style.withColor(TextColor.fromRgb(color))
            if (onClick == null && !disableBold) {
                style = style.withBold(true)
            }

            if (enableItalic) {
                style = style.withItalic(true)
            }

            val result = message.getWithStyle(style).firstOrNull()

            result ?: Text.literal(text)
        }
    var hovering = false

    init {
        width = textRenderer.getWidth(styledText)
        height = textRenderer.fontHeight
        active = onClick != null
    }

    override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(context, mouseX, mouseY, delta)
        if (!visible) {
            return
        }

        val isMouseOver = isMouseOver(mouseX.toDouble(), mouseY.toDouble())

        if (isMouseOver && !hovering) {
            hovering = true
            onMouseOn(this)
        }
        else if (!isMouseOver && hovering) {
            hovering = false
            onMouseOff(this)
        }

        val selected = isSelected(this)
        if (selected) {
            x += BORDER_BUFFER / 2
            context?.drawBorder(x, y, width, height, padding = BORDER_BUFFER)
        }

        if (!selected && showHighlight && isMouseOver) {
            context?.drawHorizontalLine(x, x + width, y + textRenderer.fontHeight, Colors.WHITE)
        }

        context?.let {
            coloredText?.let { text ->
                it.getHoverListener(this, DrawContext.HoverType.NONE)
                    .marqueedText(text, x, x, x + width, y, y + textRenderer.fontHeight)
            }
        }
    }

    override fun onClick(mouseX: Double, mouseY: Double) {
        super.onClick(mouseX, mouseY)

        if (visible && active)
        {
            onClick?.invoke(this)
        }
    }

    override fun renderButton(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
    }

    override fun appendClickableNarrations(builder: NarrationMessageBuilder?) {
    }

    fun disableBold() {
        disableBold = true
        coloredText?.let {
            this.width = textRenderer.getWidth(it)
        }
    }

    fun enableItalic() {
        enableItalic = true
        coloredText?.let {
            this.width = textRenderer.getWidth(it)
        }
    }

    fun setText(text: String) {
        message = Text.literal(text)
        coloredText?.let {
            this.width = textRenderer.getWidth(it)
        }
    }

    companion object {
        const val BORDER_BUFFER = 4
    }
}