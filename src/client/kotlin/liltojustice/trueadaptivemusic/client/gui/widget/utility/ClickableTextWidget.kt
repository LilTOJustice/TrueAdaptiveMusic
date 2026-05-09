package liltojustice.trueadaptivemusic.client.gui.widget.utility

import liltojustice.trueadaptivemusic.client.gui.extensions.drawBorder
import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.Click
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
    protected val showHighlight: Boolean = true,
    protected var onClick: ((ClickableTextWidget) -> Unit)? = null,
    protected val isSelected: (ClickableTextWidget) -> Boolean = { false },
    protected val onMouseOn: (ClickableTextWidget) -> Unit = {},
    protected val onMouseOff: (ClickableTextWidget) -> Unit = {}
): ClickableWidget(x, y, 0, 0, Text.literal(text)) {
    var color: Int = Colors.WHITE
    val text: String
        get() = message.string
    protected val textRenderer: TextRenderer = MinecraftClient.getInstance().textRenderer
    private var disableBold = false
    private var enableItalic = false
    private val coloredText: Text?
        get() = run {
            var style = message.style.withColor(TextColor.fromRgb(color))
            if (onClick == null && !disableBold) {
                style = style.withBold(true)
            }

            if (enableItalic) {
                style = style.withItalic(true)
            }

            val result = message.getWithStyle(style).firstOrNull()

            result
        }
    var hovering = false

    init {
        coloredText?.let { width = textRenderer.getWidth(it) }
        height = textRenderer.fontHeight
        active = onClick != null
    }

    override fun renderWidget(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
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

    override fun onClick(click: Click, doubled: Boolean) {
        super.onClick(click, doubled)

        if (visible && active)
        {
            onClick?.invoke(this)
        }
    }

    override fun appendClickableNarrations(builder: NarrationMessageBuilder?) {
    }

    fun withoutBold(): ClickableTextWidget {
        disableBold = true
        coloredText?.let {
            this.width = textRenderer.getWidth(it)
        }

        return this
    }

    fun withItalic(): ClickableTextWidget {
        enableItalic = true
        coloredText?.let {
            this.width = textRenderer.getWidth(it)
        }

        return this
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