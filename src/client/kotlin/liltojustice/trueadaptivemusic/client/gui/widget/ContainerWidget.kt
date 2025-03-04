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
    private val indentChildren: Boolean = true,
    x: Int = 0,
    y: Int = 0,
    private val translucentInteract: Boolean = false)
    : ClickableWidget(x, y, width, height, Text.literal(message)) {
    private val children = mutableMapOf<String, ChildWidget>()
    private val renderChildren = mutableMapOf<String, ChildWidget>()
    private val client = MinecraftClient.getInstance()
    protected val textRenderer = client.textRenderer
    protected val screen = client.currentScreen
    private var scrollPosition = 0

    override fun renderButton(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
    }

    override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        renderChildren.clear()
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
            translated.widget.x = x + translated.xOffset + if (indentChildren) X_MARGIN else 0
            translated.widget.y = getTranslatedY(translated.row)
            translated.widget.width = min(translated.widget.width, width - translated.xOffset - 2 * X_MARGIN)
            if (childVisible(translated))
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
                if (child.widget is ContainerWidget && child.widget.shouldBlockScroll(mouseX, mouseY))
                {
                    return@mouseScrolled isMouseOver(mouseX, mouseY)
                }
            }
        }

        if (!isMouseOver(mouseX, mouseY)) {
            return false
        }

        scrollPosition -= amount.toInt()

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
        row: Int? = null,
        xOffset: Int = 0,
        shouldRecompute: () -> Boolean = { false }): ClickableWidget {
        if (!children.containsKey(widgetId) || shouldRecompute()) {
            children[widgetId] = ChildWidget(widgetId, widgetMaker(), row ?: 0, xOffset, true)
        }

        if (row == null) {
            children[widgetId] = children[widgetId]!!.copy(row = maxUsedRow(true, true) + 1)
        }

        renderChildren[widgetId] = children[widgetId]!!.copy()

        return children[widgetId]!!.widget
    }

    fun addWidget(child: ClickableWidget, row: Int, xOffset: Int = 0): ClickableWidget {
        val hash = child.hashCode().toString()
        if (!children.containsKey(hash)) {
            children[hash] = ChildWidget(hash, child, row, xOffset)
        }

        return children[hash]!!.widget
    }

    // Use to only clear widgets created from addWidgetToRender
    fun clearWidgetsFromRender(keepPredicate: (childWidget: ChildWidget) -> Boolean = { false }) {
        children
            .filterValues { child -> child.fromRender }
            .forEach { (key, child) ->
                if (!keepPredicate(child))
                    children.remove(key)
            }
        renderChildren.clear()
    }

    fun clearWidgets(keepPredicate: (childWidget: ChildWidget) -> Boolean = { false }) {
        // Copy to avoid concurrent modification
        val children = children.toList()
        children
            .forEach { (key, child) ->
                if (!keepPredicate(child))
                    this.children.remove(key)
            }
        renderChildren.clear()
    }

    fun fitToUsedRows(maxRows: Int = 0) {
        height = (
                (if (maxRows > 0)
                    min(maxRows, maxUsedRow(countOffscreen = true) + 1)
                else
                    maxUsedRow(countOffscreen = true) + 1)
                * getRowHeight(textRenderer.fontHeight)
                + getHeaderOffset()).toInt()
    }

    fun fitToChildrenHeight() {
        var max = 0
        children.filterValues { child -> childVisible(child.translated(scrollPosition)) }.forEach { (_, child) ->
            val translated = child.translated(scrollPosition)
            max = max(max, getTranslatedY(translated.row) - y + translated.widget.height)
        }
        height = (max + getRowHeight(textRenderer.fontHeight)).toInt()
    }

    fun fitToChildrenWidth() {
        var max = 0
        children.forEach { (_, child) ->
            val translated = child.translated(scrollPosition)
            if (childVisible(child)) {
                max = max(max, translated.widget.x + translated.xOffset + X_MARGIN + translated.widget.width - x)
            }
        }
        width = max
    }

    fun fitToChildren() {
        fitToChildrenHeight()
        fitToChildrenWidth()
    }

    fun resetScrolling() {
        scrollPosition = 0
    }

    private fun clampScrollPosition() {
        scrollPosition = min(scrollPosition, maxUsedRow(countOffscreen = true) + 1 - totalRows())
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

    private fun maxUsedRow(onlyThisRender: Boolean = false, countOffscreen: Boolean = false): Int {
        return if (visible) (if (onlyThisRender) renderChildren else children)
            .mapValues { (_, child) -> if (countOffscreen) child else child.translated(scrollPosition) }
            .filterValues { child ->
                if (countOffscreen) child.widget.visible else childVisible(child) }
            .maxOfOrNull { (_, child) ->
                child.row + if (child.widget is ContainerWidget) child.widget.maxUsedRow() + 1 else 0 }
            ?: 0 else 0
    }

    private fun drawScrollBar(context: DrawContext?) {
        val usedRows = maxUsedRow(countOffscreen = true) + 1
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

    private fun childVisible(translated: ChildWidget): Boolean {
        return translated.widget.visible && translated.row >= 0 && translated.row < totalRows()
    }

    private fun shouldBlockScroll(mouseX: Double, mouseY: Double): Boolean {
        return (!translucentInteract && visible && active && isMouseOver(mouseX, mouseY))
                || children.any { (_, child) ->
                    child.widget is ContainerWidget && child.widget.shouldBlockScroll(mouseX, mouseY) }
    }

    companion object {
        private const val TOP_MARGIN = 12
        private const val X_MARGIN = 5
        private fun getRowHeight(fontHeight: Int): Double {
            return (1.35 * fontHeight)
        }
    }

    data class ChildWidget(
        val id: String, val widget: ClickableWidget, val row: Int, val xOffset: Int, val fromRender: Boolean = false) {
        fun translated(row: Int, xOffset: Int = 0): ChildWidget {
            return copy(row = this.row - row, xOffset = this.xOffset + xOffset)
        }
    }
}