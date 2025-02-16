package liltojustice.trueadaptivemusic.client.gui.widget

import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.Screen.OPTIONS_BACKGROUND_TEXTURE
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.text.Text
import net.minecraft.util.Colors
import java.util.function.Consumer

abstract class ContainerWidget(
    private val parentScreen: Screen,
    width: Int,
    height: Int,
    message: String = "",
    private var showHeader: Boolean = false,
    x: Int = 0,
    y: Int = 0)
    : ClickableWidget(x, y, width, height, Text.literal(message)) {
    private val children = mutableListOf<ChildWidget>()
    private val client = MinecraftClient.getInstance()
    private val textRenderer = client.textRenderer

    override fun renderButton(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        render(context, mouseX, mouseY, delta)
    }

    override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        context?.setShaderColor(0.125f, 0.125f, 0.125f, 1.0f)
        context?.drawTexture(
            OPTIONS_BACKGROUND_TEXTURE, x, y, 0F, 0F, width, height, 32, 32
        )
        context?.setShaderColor(1f, 1f, 1f, 1f)

        if (showHeader)
        {
            context?.setShaderColor(0.05f, 0.05f, 0.05f, 1.0f)
            context?.drawTexture(
                OPTIONS_BACKGROUND_TEXTURE, x, y, 0F, 0F, width, TOP_MARGIN, 32, 32)
            context?.setShaderColor(1f, 1f, 1f, 1f)
            drawCenteredText(context, message.string, -1, width / 2, shadow = true)
        }
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        children.forEach { child ->
            if (child.widget.isMouseOver(mouseX, mouseY)) {
                child.widget.mouseClicked(mouseX, mouseY, button)
            }
        }
        return clicked(mouseX, mouseY)
    }

    protected fun drawText(
        drawContext: DrawContext?,
        text: String,
        row: Int,
        xOffset: Int = 0,
        color: Int = Colors.WHITE,
        shadow: Boolean = true) {
        drawContext?.drawText(
            textRenderer,
            text,
            X_MARGIN + xOffset + x,
            getTranslatedY(row),
            color,
            shadow)
    }

    protected fun drawCenteredText(
        drawContext: DrawContext?,
        text: String,
        row: Int,
        xOffset: Int = 0,
        color: Int = Colors.WHITE,
        shadow: Boolean = true) {
        drawContext?.drawText(
            textRenderer,
            text,
            xOffset + x - textRenderer.getWidth(text) / 2,
            getTranslatedY(row),
            color,
            shadow)
    }

    fun addWidget(child: ClickableWidget, row: Int, xOffset: Int, shouldReinit: Boolean = false) {
        child.x = x + xOffset
        child.y = getTranslatedY(row)
        children.add(ChildWidget(child, row, xOffset))

        if (shouldReinit) {
            reinitializeScreen()
        }
    }

    fun refreshPositions() {
        val oldChildren = children.toList()
        children.clear()
        oldChildren.forEach { child -> addWidget(child.widget, child.row, child.xOffset) }
    }

    override fun forEachChild(consumer: Consumer<ClickableWidget>?) {
        super.forEachChild(consumer)
        children.forEach { child -> consumer?.accept(child.widget) }
    }

    fun reinitializeScreen() {
        client.currentScreen?.resize(client, parentScreen.width, parentScreen.height)
    }

    companion object {
        private const val TOP_MARGIN = 12
        private const val X_MARGIN = 5
    }

    private fun getHeaderOffset(): Int {
        return (if (showHeader) TOP_MARGIN else 0) + 2
    }

    private fun getTranslatedY(row: Int): Int {
        return ((row + row * 0.3) * textRenderer.fontHeight).toInt() + getHeaderOffset() + y
    }

    class ChildWidget(val widget: ClickableWidget, val row: Int, val xOffset: Int) {}
}