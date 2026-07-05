package liltojustice.trueadaptivemusic.common.client.gui.widget.utility

import liltojustice.trueadaptivemusic.common.client.gui.extensions.drawBorder
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.TextColor
import net.minecraft.util.CommonColors

open class ClickableTextDisplayWidget(
    text: String,
    x: Int = 0,
    y: Int = 0,
    private val onClick: (ClickableTextDisplayWidget) -> Unit = {})
    : AbstractWidget(x, y, 0, 0, Component.literal(text)) {
    private val font = Minecraft.getInstance().font
    var color: Int = CommonColors.WHITE
    val text: String
        get() = message.string
    val coloredText: Component?
        get() = message.toFlatList(message.style.withColor(TextColor.fromRgb(color))).firstOrNull()

    init {
        width = font.width(message)
        height = font.lineHeight
    }

    override fun extractWidgetRenderState(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, a: Float) {
        if (!visible) {
            return
        }

        graphics.drawBorder(x + TEXT_OFFSET, y, width - TEXT_OFFSET / 2, height, padding = BORDER_BUFFER)

        x += TEXT_OFFSET
        coloredText?.let {
            extractScrollingStringOverContents(
                graphics.textRendererForWidget(
                    this, GuiGraphicsExtractor.HoveredTextEffects.NONE),
                it,
                0
            )
        }
        x -= TEXT_OFFSET
    }

    override fun onClick(event: MouseButtonEvent, doubled: Boolean) {
        super.onClick(event, doubled)

        if (visible && active)
        {
            onClick(this)
        }
    }

    override fun updateWidgetNarration(output: NarrationElementOutput) {
    }

    fun setText(text: String) {
        message = Component.literal(text)
        this.width = font.width(message)
    }

    companion object {
        const val BORDER_BUFFER = 4
        const val TEXT_OFFSET = 2
    }
}