package liltojustice.trueadaptivemusic.client.gui.widget

import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen.OPTIONS_BACKGROUND_TEXTURE
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.text.Text
import net.minecraft.util.Colors
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

abstract class ContainerWidget(
    width: Int,
    height: Int,
    message: String,
    private var showHeader: Boolean,
    private var bordered: Boolean,
    x: Int = 0,
    y: Int = 0,
    private val translucentInteract: Boolean = true)
    : ClickableWidget(x, y, width, height, Text.literal(message)) {
    private val children = mutableMapOf<String, ChildWidget>()
    private val client = MinecraftClient.getInstance()
    protected val textRenderer = client.textRenderer
    protected val screen = client.currentScreen
    private var scrollPosition = 0

    override fun renderButton(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        render(context, mouseX, mouseY, delta)
    }

    override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        if (!visible) {
            return
        }

        if (bordered) {
            context?.setShaderColor(0f, 0f, 0f, 1f)
            context?.drawTexture(
                OPTIONS_BACKGROUND_TEXTURE, x, y, 0f, 0f, width, height, 32, 32
            )
        }
        else {
            context?.setShaderColor(0.125f, 0.125f, 0.125f, 1.0f)
            context?.drawTexture(
                OPTIONS_BACKGROUND_TEXTURE, x, y, 0f, 0f, width, height, 32, 32
            )
        }
        context?.setShaderColor(1f, 1f, 1f, 1f)

        if (showHeader)
        {
            context?.setShaderColor(0.05f, 0.05f, 0.05f, 1.0f)
            context?.drawTexture(
                OPTIONS_BACKGROUND_TEXTURE, x, y, 0F, 0F, width, TOP_MARGIN, 32, 32)
            context?.setShaderColor(1f, 1f, 1f, 1f)
            drawCenteredText(context, message.string, -1, width / 2, shadow = true)
        }

        if (bordered) {
            context?.drawBorder(x, y, width, height, Colors.WHITE)
        }

        clampScrollPosition()
        drawScrollBar(context)

        children.forEach { (_, child) ->
            val translated = child.translated(scrollPosition)
            translated.widget.x = x + translated.xOffset + X_MARGIN
            translated.widget.y = getTranslatedY(translated.row)
            if (translated.row >= 0 && translated.row < totalRows())
            {
                translated.widget.render(context, mouseX, mouseY, delta)
            }
        }
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (!visible || !active) {
            return false
        }

        // Copy to avoid concurrent modification
        val children = children.toList()
        screen?.focused = null
        children.forEach { (_, child) ->
            if (child.widget.isMouseOver(mouseX, mouseY)) {
                val clicked = child.widget.mouseClicked(mouseX, mouseY, button)
                if (clicked) {
                    screen?.focused = if (screen?.focused != null) screen.focused else child.widget
                }
            }
        }

        return false
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, amount: Double): Boolean {
        if (!visible || !active) {
            return false
        }

        // Copy to avoid concurrent modification
        val children = children.toList()
        children.forEach { (_, child) ->
            if (child.widget.isMouseOver(mouseX, mouseY)) {
                child.widget.mouseScrolled(mouseX, mouseY, amount)
                if (translucentInteract)
                {
                    return@forEach
                }

                return@mouseScrolled true
            }
        }

        if (!isMouseOver(mouseX, mouseY)) {
            return false
        }

        scrollPosition -= amount.toInt()
        clampScrollPosition()

        return true
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

    // Use if the widget is created on render
    fun addWidgetFromRender(
        widgetMaker: () -> ClickableWidget,
        widgetId: String,
        row: Int,
        xOffset: Int = 0,
        shouldRecompute: () -> Boolean = { false }) {
        if (!children.containsKey(widgetId) || shouldRecompute()) {
            children[widgetId] = ChildWidget(widgetMaker(), row, xOffset, true)
        }
    }

    fun addWidget(child: ClickableWidget, row: Int, xOffset: Int = 0) {
        val hash = child.hashCode().toString()
        if (!children.containsKey(hash)) {
            children[hash] = ChildWidget(child, row, xOffset)
        }
    }

    // Use to only clear widgets created from addWidgetToRender
    fun clearWidgetsFromRender() {
        children.filterValues { child -> child.fromRender }.forEach { (key, _) -> children.remove(key) }
    }

    fun clearWidgets() {
        children.clear()
    }

    fun fitToUsedRows(maxRows: Int = 0) {
        height = ((if (maxRows > 0) min(maxRows, maxUsedRow() + 1) else maxUsedRow() + 1)
                * getRowHeight(textRenderer.fontHeight)
                + getHeaderOffset()).toInt()
    }

    fun fitToChildrenHeight() {
        var max = 0
        children.forEach { (_, child) ->
            val translated = child.translated(scrollPosition)
            translated.widget.y = getTranslatedY(translated.row)
            max = max(max, (translated.widget.y + translated.widget.height) - y)
        }
        height = (max + getRowHeight(textRenderer.fontHeight)).toInt()
    }

    fun fitToChildrenWidth() {
        var max = 0
        children.forEach { (_, child) ->
            val translated = child.translated(scrollPosition)
            translated.widget.x = x + translated.xOffset + X_MARGIN
            max = max(max, (translated.widget.x + translated.widget.width) - x)
        }
        width = max
    }

    fun fitToChildren() {
        fitToChildrenHeight()
        fitToChildrenWidth()
    }

    private fun clampScrollPosition() {
        scrollPosition = min(scrollPosition, (maxUsedRow() + 1) - totalRows())
        scrollPosition = max(0, scrollPosition)
    }

    private fun getHeaderOffset(): Int {
        return (if (showHeader) TOP_MARGIN else 0) + 2
    }

    private fun getTranslatedY(row: Int): Int {
        return (row * getRowHeight(textRenderer.fontHeight)).toInt() + getHeaderOffset() + y
    }

    private fun totalRows(): Int {
        return ((height - getHeaderOffset()) / getRowHeight(textRenderer.fontHeight)).roundToInt()
    }

    private fun maxUsedRow(): Int {
        return children
            .filterValues { child -> child.widget.visible }.maxByOrNull { (_, child) -> child.row }?.value?.row ?: 0
    }

    private fun drawScrollBar(context: DrawContext?) {
        val usedRows = maxUsedRow() + 1
        val totalRows = totalRows()
        if (usedRows > totalRows) {
            val adjustedHeight = height - getHeaderOffset() - 2
            val ratio = totalRows.toDouble() / usedRows
            val barSize = ratio * adjustedHeight
            val start = (scrollPosition.toDouble() / (usedRows - totalRows)) * adjustedHeight * (1 - ratio)
            val end = start + barSize
            context?.drawVerticalLine(
                x + width - 3,
                (y + start + getHeaderOffset()).toInt(),
                (y + end + getHeaderOffset()).toInt(),
                Colors.WHITE)
        }
    }

    companion object {
        private const val TOP_MARGIN = 12
        private const val X_MARGIN = 5
        private fun getRowHeight(fontHeight: Int): Double {
            return (1.35 * fontHeight)
        }
    }

    data class ChildWidget(
        val widget: ClickableWidget, val row: Int, val xOffset: Int, val fromRender: Boolean = false) {
        fun translated(row: Int, xOffset: Int = 0): ChildWidget {
            return copy(row = this.row - row, xOffset = this.xOffset + xOffset)
        }
    }
}