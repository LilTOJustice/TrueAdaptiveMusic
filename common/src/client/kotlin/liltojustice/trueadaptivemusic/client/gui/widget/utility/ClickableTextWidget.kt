package liltojustice.trueadaptivemusic.client.gui.widget.utility

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.TextColor
import net.minecraft.util.CommonColors

open class ClickableTextWidget(
    text: String,
    x: Int = 0,
    y: Int = 0,
    protected val showHighlight: Boolean = true,
    protected var onClick: ((ClickableTextWidget) -> Unit)? = null,
    protected val isHoveredOrFocused: (ClickableTextWidget) -> Boolean = { false },
    protected val onMouseOn: (ClickableTextWidget) -> Unit = {},
    protected val onMouseOff: (ClickableTextWidget) -> Unit = {}
): AbstractWidget(x, y, 0, 0, Component.literal(text)) {
    var color: Int = CommonColors.WHITE
    val text: String
        get() = message.string
    protected val textRenderer: Font = Minecraft.getInstance().font
    private var disableBold = false
    private var enableItalic = false
    private val styledText: Component
        get() = run {
            var style = message.style.withColor(TextColor.fromRgb(color))
            if (onClick == null && !disableBold) {
                style = style.withBold(true)
            }

            if (enableItalic) {
                style = style.withItalic(true)
            }

            val result = message.toFlatList(style).firstOrNull()

            result ?: Component.literal(text)
        }
    var hovering = false

    init {
        width = textRenderer.width(styledText)
        height = textRenderer.lineHeight
        active = onClick != null
    }

    override fun renderWidget(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
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

        val selected = isHoveredOrFocused(this)
        if (selected) {
            x += BORDER_BUFFER / 2
            context.renderOutline(
                x - BORDER_BUFFER / 2,
                y - BORDER_BUFFER / 2,
                width + BORDER_BUFFER,
                height + BORDER_BUFFER,
                CommonColors.WHITE
            )
        }

        if (!selected && showHighlight && isMouseOver) {
            context.hLine(x, x + width, y + textRenderer.lineHeight, CommonColors.WHITE)
        }

        context.let {
            renderScrollingString(
                it,
                textRenderer,
                styledText,
                x,
                y,
                x + width,
                y + textRenderer.lineHeight,
                CommonColors.WHITE
            )
        }
    }

    override fun onClick(mouseX: Double, mouseY: Double) {
        super.onClick(mouseX, mouseY)

        if (visible && active)
        {
            onClick?.invoke(this)
        }
    }

    override fun updateWidgetNarration(builder: NarrationElementOutput) {
    }

    fun withoutBold(): ClickableTextWidget {
        disableBold = true
        this.width = textRenderer.width(styledText)

        return this
    }

    fun withItalic(): ClickableTextWidget {
        enableItalic = true
        this.width = textRenderer.width(styledText)

        return this
    }

    fun setText(text: String) {
        message = Component.literal(text)
        this.width = textRenderer.width(styledText)
    }

    companion object {
        const val BORDER_BUFFER = 4
    }
}