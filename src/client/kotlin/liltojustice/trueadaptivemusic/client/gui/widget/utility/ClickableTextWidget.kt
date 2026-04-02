package liltojustice.trueadaptivemusic.client.gui.widget.utility

import liltojustice.trueadaptivemusic.client.gui.extensions.drawBorder
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.TextColor
import net.minecraft.util.CommonColors

open class ClickableTextWidget(
    text: String,
    x: Int = 0,
    y: Int = 0,
    protected val showHighlight: Boolean = true,
    protected var onClick: ((ClickableTextWidget) -> Unit)? = null,
    protected val isSelected: (ClickableTextWidget) -> Boolean = { false },
    protected val onMouseOn: (ClickableTextWidget) -> Unit = {},
    protected val onMouseOff: (ClickableTextWidget) -> Unit = {}
): AbstractWidget(x, y, 0, 0, Component.literal(text)) {
    var color: Int = CommonColors.WHITE
    val text: String
        get() = message.string
    protected val font: Font = Minecraft.getInstance().font
    private var disableBold = false
    private var enableItalic = false
    private val coloredText: Component?
        get() = run {
            var style = message.style.withColor(TextColor.fromRgb(color))
            if (onClick == null && !disableBold) {
                style = style.withBold(true)
            }

            if (enableItalic) {
                style = style.withItalic(true)
            }

            val result = message.toFlatList(style).firstOrNull()

            result
        }
    var hovering = false

    init {
        coloredText?.let { width = font.width(it) }
        height = font.lineHeight
        active = onClick != null
    }

    override fun extractWidgetRenderState(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, a: Float) {
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
            graphics.drawBorder(x, y, width, height, padding = BORDER_BUFFER)
        }

        if (!selected && showHighlight && isMouseOver) {
            graphics.horizontalLine(x, x + width, y + font.lineHeight, CommonColors.WHITE)
        }

        coloredText?.let { text ->
            graphics.textRendererForWidget(this, GuiGraphicsExtractor.HoveredTextEffects.NONE)
                .acceptScrolling(
                    text, x, x, x + width, y, y + font.lineHeight)
        }
    }

    override fun onClick(event: MouseButtonEvent, doubleClick: Boolean) {
        super.onClick(event, doubleClick)

        if (visible && active)
        {
            onClick?.invoke(this)
        }
    }

    override fun updateWidgetNarration(output: NarrationElementOutput) {
    }

    fun disableBold() {
        disableBold = true
        coloredText?.let {
            this.width = font.width(it)
        }
    }

    fun enableItalic() {
        enableItalic = true
        coloredText?.let {
            this.width = font.width(it)
        }
    }

    fun setText(text: String) {
        message = Component.literal(text)
        coloredText?.let {
            this.width = font.width(it)
        }
    }

    companion object {
        const val BORDER_BUFFER = 4
    }
}