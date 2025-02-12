package liltojustice.trueadaptivemusic.client.gui.widget

import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen.OPTIONS_BACKGROUND_TEXTURE
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.client.gui.widget.Widget
import net.minecraft.text.Text
import net.minecraft.util.Colors

abstract class ContainerWidget(
    width: Int,
    height: Int,
    message: String = "",
    private var showHeader: Boolean = false,
    x: Int = 0,
    y: Int = 0)
    : ClickableWidget(x, y, width, height, Text.literal(message)) {
    private val children = mutableListOf<Widget>()

    override fun renderButton(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        render(context, mouseX, mouseY, delta)
    }

    override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        context?.setShaderColor(0.125f, 0.125f, 0.125f, 1.0f)
        context?.drawTexture(
            OPTIONS_BACKGROUND_TEXTURE,
            x,
            y,
            0F,
            0F,
            width,
            height,
            32,
            32
        )
        context?.setShaderColor(1f, 1f, 1f, 1f)

        if (showHeader)
        {
            context?.setShaderColor(0.05f, 0.05f, 0.05f, 1.0f)
            context?.drawTexture(
                OPTIONS_BACKGROUND_TEXTURE,
                x,
                y,
                0F,
                0F,
                width,
                TOP_MARGIN,
                32,
                32
            )
            context?.setShaderColor(1f, 1f, 1f, 1f)
            val textRenderer = MinecraftClient.getInstance().textRenderer
            drawCenteredText(
                context,
                textRenderer,
                message.string,
                -1,
                width / 2,
                shadow = true)
        }
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        return clicked(mouseX, mouseY)
    }

    protected fun drawText(
        drawContext: DrawContext?,
        textRenderer: TextRenderer,
        text: String,
        row: Int,
        xOffset: Int = 0,
        color: Int = Colors.WHITE,
        shadow: Boolean = false) {
        drawContext?.drawText(
            textRenderer,
            text,
            X_MARGIN + xOffset + x,
            ((row + row * 0.3) * textRenderer.fontHeight).toInt() + getHeaderOffset() + y,
            color,
            shadow)
    }

    protected fun drawCenteredText(
        drawContext: DrawContext?,
        textRenderer: TextRenderer,
        text: String,
        row: Int,
        xOffset: Int = 0,
        color: Int = Colors.WHITE,
        shadow: Boolean = false) {
        drawContext?.drawText(
            textRenderer,
            text,
            xOffset + x - textRenderer.getWidth(text) / 2,
            ((row + row * 0.3) * textRenderer.fontHeight).toInt() + getHeaderOffset() + y,
            color,
            shadow)
    }

    open fun addChild(child: Widget) {
        children.add(child)
    }

    companion object {
        private const val TOP_MARGIN = 12
        private const val X_MARGIN = 5
    }

    private fun getHeaderOffset(): Int {
        return (if (showHeader) TOP_MARGIN else 0) + 2
    }
}