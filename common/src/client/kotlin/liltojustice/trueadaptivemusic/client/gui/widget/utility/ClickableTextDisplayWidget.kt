package liltojustice.trueadaptivemusic.client.gui.widget.utility

import liltojustice.trueadaptivemusic.client.gui.extensions.drawBorder
import net.minecraft.client.Minecraft
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.TextColor
import net.minecraft.util.CommonColors

open class ClickableTextDisplayWidget(
    text: String,
    x: Int = 0,
    y: Int = 0,
    private val onClick: (ClickableTextDisplayWidget) -> Unit = {})
    : AbstractWidget(x, y, 0, 0, Component.literal(text)) {
    private val textRenderer = Minecraft.getInstance().font
    var color: Int = CommonColors.WHITE
    val text: String
        get() = message.string
    val coloredText: Component?
        get() = message.toFlatList(message.style.withColor(TextColor.fromRgb(color))).firstOrNull()

    init {
        width = textRenderer.width(message)
        height = textRenderer.lineHeight
    }

    override fun renderWidget(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        if (!visible) {
            return
        }

        context.drawBorder(x + TEXT_OFFSET, y, width - TEXT_OFFSET / 2, height, padding = BORDER_BUFFER)

        x += TEXT_OFFSET
        context.let {
            coloredText?.let {
                renderScrollingStringOverContents(
                    context.textRendererForWidget(this, GuiGraphics.HoveredTextEffects.NONE),
                    it,
                    0
                )
            }
        }
        x -= TEXT_OFFSET
    }

    override fun onClick(click: MouseButtonEvent, doubled: Boolean) {
        super.onClick(click, doubled)

        if (visible && active)
        {
            onClick(this)
        }
    }

    override fun updateWidgetNarration(builder: NarrationElementOutput) {
    }

    fun setText(text: String) {
        message = Component.literal(text)
        this.width = textRenderer.width(message)
    }

    companion object {
        const val BORDER_BUFFER = 4
        const val TEXT_OFFSET = 2
    }
}