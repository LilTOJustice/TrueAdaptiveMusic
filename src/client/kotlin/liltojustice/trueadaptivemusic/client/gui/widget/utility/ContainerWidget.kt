package liltojustice.trueadaptivemusic.client.gui.widget.utility

import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.Screen.MENU_BACKGROUND_TEXTURE
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.client.sound.SoundManager
import net.minecraft.screen.ScreenTexts
import net.minecraft.text.Text
import net.minecraft.util.Colors
import net.minecraft.util.Identifier
import java.util.function.Consumer
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

abstract class ContainerWidget(
    width: Int,
    height: Int,
    message: String,
    private val showHeader: Boolean,
    private val bordered: Boolean,
    private val verticallyScrollable: Boolean = false,
    private val horizontallyScrollable: Boolean = false,
    private val indentChildren: Boolean = true,
    x: Int = 0,
    y: Int = 0,
    private val translucentInteract: Boolean = false,
    backButtonCallback: (() -> Unit)? = null)
    : ClickableWidget(x, y, width, height, Text.literal(message)) {
    private val children = mutableMapOf<String, ChildWidget>()
    private val renderChildren = mutableMapOf<String, ChildWidget>()
    private val client = MinecraftClient.getInstance()
    protected val textRenderer: TextRenderer = client.textRenderer
    protected val screen: Screen? = client.currentScreen
    private var verticalScrollPosition = 0.0
    private var horizontalScrollPosition = 0.0
    private var verticalScrollHeld = false
    private var horizontalScrollHeld = false
    private var backButton = backButtonCallback?.let { makeBackButton(it) }
    private var lastUsedWidth = 0
    private var renderWidgetClearQueue = mutableListOf<(ChildWidget) -> Boolean>()
    var focusedWidget: ClickableWidget? = null
        protected set

    fun addBackButton(backButtonCallback: (() -> Unit)) {
        backButton = makeBackButton(backButtonCallback)
    }

    override fun appendClickableNarrations(builder: NarrationMessageBuilder?) {
    }

    override fun playDownSound(soundManager: SoundManager?) {
    }

    override fun setHeight(height: Int) {
        this.height = height
    }

    override fun renderWidget(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        renderWidgetClearQueue.forEach { clearWidgetsFromRender(it) }
        renderWidgetClearQueue.clear()
        focusedWidget?.isFocused = true
        renderChildren.clear()
        if (!visible) {
            return
        }

        //super.render(context, mouseX, mouseY, delta)

        if (showHeader)
        {
            context?.setShaderColor(0f, 0f, 0f, if (bordered) 1f else 0.5f)
            context?.fill(x, y, x + this.width, y + this.height, Colors.BLACK)
            context?.fill(x, y,  x + width, y + TOP_MARGIN, Colors.BLACK)
            context?.setShaderColor(1f, 1f, 1f, 1f)
            drawCenteredText(context, message.string, -1, width / 2, shadow = true)
            backButton?.let {
                it.x = x + 5
                it.y = (y + getHeaderOffset() - getRowHeight(textRenderer.fontHeight)).toInt()
                it.render(context, mouseX, mouseY, delta)
            }
            context?.setShaderColor(1f, 1f, 1f, 1f)
        }

       if (bordered) {
            context?.drawBorder(x, y, width, height, Colors.WHITE)
        }

        val usedWidth = getMaxUsedWidth()
        if (lastUsedWidth == usedWidth) {
            clampScrollPosition()
        }

        val verticalExtent = drawVerticalScrollbar(context)
        val horizontalExtent = drawHorizontalScrollbar(context)

        context?.enableScissor(
            x + if (indentChildren) X_MARGIN else 0,
            y + getHeaderOffset() - 2,
            (verticalExtent?.third ?: (x + width)) - 2,
            (horizontalExtent?.third ?: (y + height)) - 2
        )
        children.forEach { (_, child) ->
            val translated = child.translated(
                verticalScrollPosition.toInt(), -horizontalScrollPosition.toInt())
            translated.widget.x = x + translated.xOffset + (if (indentChildren) X_MARGIN else 0)
            translated.widget.y = getTranslatedY(translated.row)

            if (!horizontallyScrollable) {
                translated.widget.width = min(
                    translated.widget.width, width - translated.xOffset - 2 * X_MARGIN)
            }

            translated.widget.render(context, mouseX, mouseY, delta)
        }

        context?.disableScissor()
        lastUsedWidth = getMaxUsedWidth()
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (!visible ||
            !active ||
            !this.isValidClickButton(button) ||
            !isMouseOver(mouseX, mouseY)) {
            unfocus()
            return false
        }

        backButton?.let {
            if (it.mouseClicked(mouseX, mouseY, button)) {
                return true
            }
        }

        val verticalScrollExtent = getVerticalScrollbarExtent()
        val horizontalScrollExtent = getHorizontalScrollbarExtent()

        verticalScrollExtent?.let {
            if (mouseY >= it.first - SCROLLBAR_GRACE &&
                mouseY <= it.second + SCROLLBAR_GRACE &&
                abs(mouseX - it.third) <= SCROLLBAR_GRACE) {
                verticalScrollHeld = true
                screen?.focused = this

                return true
            }
        }

        horizontalScrollExtent?.let {
            if (mouseX >= it.first - SCROLLBAR_GRACE &&
                mouseX <= it.second + SCROLLBAR_GRACE &&
                abs(mouseY - it.third) <= SCROLLBAR_GRACE) {
                horizontalScrollHeld = true
                screen?.focused = this

                return true
            }
        }

        focusedWidget = null
        // Copy to avoid concurrent modification
        val children = children.toList()
        children.forEach { (_, child) ->
            if (child.widget.mouseClicked(mouseX, mouseY, button)) {
                focusedWidget = child.widget
            }
            else {
                child.widget.isFocused = false
            }
        }

        screen?.focused = this

        return true
    }

    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, deltaX: Double, deltaY: Double): Boolean {
        if (verticalScrollHeld) {
            val usableHeight = getUsableHeight()
            getVerticalScrollbarExtent()?.let {
                val ratio = usableHeight.toDouble() / (it.second - it.first)
                verticalScrollPosition += (deltaY * ratio) / getRowHeight(textRenderer.fontHeight)
            }
        }
        else if (horizontalScrollHeld) {
            val usableWidth = getUsableWidth()
            getHorizontalScrollbarExtent()?.let {
                val ratio = usableWidth.toDouble() / (it.second - it.first)
                horizontalScrollPosition += deltaX * ratio
            }
        }

        return focusedWidget?.mouseDragged(mouseX, mouseY, button, deltaX, deltaY) ?: false
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (!visible || !active || !this.isValidClickButton(button)) {
            return false
        }

        verticalScrollHeld = false
        horizontalScrollHeld = false

        return focusedWidget?.mouseReleased(mouseX, mouseY, button) ?: true
    }

    override fun charTyped(chr: Char, modifiers: Int): Boolean {
        return focusedWidget?.charTyped(chr, modifiers) ?: false
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        return focusedWidget?.keyPressed(keyCode, scanCode, modifiers) ?: false
    }

    override fun keyReleased(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        return focusedWidget?.keyReleased(keyCode, scanCode, modifiers) ?: false
    }

    override fun mouseScrolled(
        mouseX: Double, mouseY: Double, horizontalAmount: Double, verticalAmount: Double): Boolean {
        if (!visible || !active) {
            return false
        }

        // Copy to avoid concurrent modification
        val children = children.toList()
        children.forEach { (_, child) ->
            if (child.widget.isMouseOver(mouseX, mouseY)) {
                child.widget.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)
                if (child.widget is ContainerWidget && child.widget.shouldBlockScroll(mouseX, mouseY))
                {
                    return@mouseScrolled isMouseOver(mouseX, mouseY)
                }
            }
        }

        if (!isMouseOver(mouseX, mouseY)) {
            return false
        }

        if (verticallyScrollable) {
            verticalScrollPosition -= verticalAmount.toInt()
        }

        if (horizontallyScrollable) {
            horizontalScrollPosition -= horizontalAmount.toInt()
        }

        return true
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
            children[widgetId] = children[widgetId]!!.copy(
                row = getMaxUsedRow(onlyThisRender = true, countOffscreen = true))
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
        val toRemove = children
            .filterValues { child -> child.fromRender && !keepPredicate(child) }

        toRemove.forEach { (key, _) ->
            children.remove(key)
        }

        renderChildren.clear()
    }

    fun queueClearWidgetsFromRender(keepPredicate: (childWidget: ChildWidget) -> Boolean = { false }) {
        renderWidgetClearQueue.add(keepPredicate)
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
                    min(maxRows, getMaxUsedRow(countOffscreen = true) + 1)
                else
                    getMaxUsedRow(countOffscreen = true) + 1)
                        * getRowHeight(textRenderer.fontHeight)
                        + getHeaderOffset()).toInt()
    }

    fun fitToChildrenHeight() {
        var max = 0
        children
            .filterValues { child -> childVisible(child.translated(verticalScrollPosition.toInt())) }
            .forEach { (_, child) ->
                val translated = child.translated(verticalScrollPosition.toInt())
                max = max(max, getTranslatedY(translated.row) - y + translated.widget.height)
            }

        height = (max + getRowHeight(textRenderer.fontHeight)).toInt()
    }

    fun resetScrolling() {
        verticalScrollPosition = 0.0
        horizontalScrollPosition = 0.0
    }

    fun scrollToBottom() {
        verticalScrollPosition = Double.MAX_VALUE
        horizontalScrollPosition = 0.0
    }

    override fun forEachChild(consumer: Consumer<ClickableWidget>?) {
        children.values.map { child -> child.widget }.forEach(consumer)
    }

    protected open fun renderDarkening(context: DrawContext, x: Int, y: Int, width: Int, height: Int) {
        renderBackgroundTexture(
            context,
            MENU_BACKGROUND_TEXTURE,
            x,
            y,
            0.0f,
            0.0f,
            width,
            height
        )
    }

    fun renderBackgroundTexture(
        context: DrawContext,
        texture: Identifier?,
        x: Int,
        y: Int,
        u: Float,
        v: Float,
        width: Int,
        height: Int
    ) {
        context.drawTexture(
            texture,
            x,
            y,
            u,
            v,
            width,
            height,
            32,
            32
        )
    }

    private fun clampScrollPosition() {
        verticalScrollPosition = min(
            verticalScrollPosition, (getMaxUsedRow(countOffscreen = true) - totalRows()).toDouble())
        verticalScrollPosition = max(0.0, verticalScrollPosition)
        horizontalScrollPosition = min(
            horizontalScrollPosition, getMaxUsedWidth().toDouble() - getUsableWidth() - 1)
        horizontalScrollPosition = max(0.0, horizontalScrollPosition)
    }

    private fun getHeaderOffset(): Int {
        return (if (showHeader) TOP_MARGIN else 0) + 2
    }

    private fun getTranslatedY(row: Int): Int {
        return (row * getRowHeight(textRenderer.fontHeight)).toInt() + getHeaderOffset() + y
    }

    private fun totalRows(): Int {
        return ((height - getHeaderOffset()) / getRowHeight(textRenderer.fontHeight)).roundToInt() -
                (if (horizontallyScrollable) 1 else 0)
    }

    private fun getUsableHeight(): Int {
        return height - getHeaderOffset() -
                if (horizontallyScrollable) getHorizontalScrollbarYPosition() - (y + height) else 0
    }

    private fun getUsableWidth(): Int {
        return width - 2 * X_MARGIN - if (verticallyScrollable) getVerticalScrollbarXPosition() - (x + width) else 0
    }

    private fun getMaxUsedRow(onlyThisRender: Boolean = false, countOffscreen: Boolean = false): Int {
        return if (visible) (if (onlyThisRender) renderChildren else children)
            .mapValues { (_, child) ->
                if (countOffscreen) child else child.translated(verticalScrollPosition.toInt()) }
            .filterValues { child ->
                if (countOffscreen) child.widget.visible else childVisible(child) }
            .maxOfOrNull { (_, child) ->
                child.row + if (child.widget is ContainerWidget) child.widget.getMaxUsedRow() + 1 else 1 }
            ?: 0 else 0
    }

    private fun getMaxUsedWidth(): Int {
        return children.values.maxOfOrNull { it.xOffset + it.widget.width + X_MARGIN } ?: width
    }

    private fun drawVerticalScrollbar(context: DrawContext?): Triple<Int, Int, Int>? {
        val extent = getVerticalScrollbarExtent() ?: return null
        val headerOffset = getHeaderOffset()
        val adjustedHeight = height - headerOffset - 6
        val x = extent.third

        context?.let {
            renderDarkening(it, x, y + headerOffset, 1, adjustedHeight)
            it.drawVerticalLine(
                x,
                extent.first,
                extent.second,
                Colors.WHITE
            )
        }

        return extent
    }

    private fun drawHorizontalScrollbar(context: DrawContext?): Triple<Int, Int, Int>? {
        val extent = getHorizontalScrollbarExtent() ?: return null
        val usableWidth = getUsableWidth()
        val offset = width - usableWidth
        val y = extent.third

        context?.let {
            renderDarkening(it, x + offset, y, usableWidth - offset, 1)
            it.drawHorizontalLine(extent.first, extent.second, y, Colors.WHITE)
        }

        return extent
    }

    private fun getVerticalScrollbarExtent(): Triple<Int, Int, Int>? {
        if (!verticallyScrollable) {
            return null
        }

        val usedRows = getMaxUsedRow(countOffscreen = true)
        val totalRows = totalRows()

        if (usedRows <= totalRows) {
            return null
        }

        val headerOffset = getHeaderOffset()
        val adjustedHeight = height - headerOffset - 6
        val ratio = totalRows.toDouble() / usedRows
        val barSize = ratio * adjustedHeight
        val start = (verticalScrollPosition / (usedRows - totalRows)) * adjustedHeight * (1 - ratio)
        val end = start + barSize
        val y1 = (y + start + headerOffset).toInt()
        val y2 = (y + end + headerOffset).toInt()
        val diff = y2 - y1

        return Triple(y1, if (diff < 2) y2+ (2 - diff) else y2, getVerticalScrollbarXPosition())
    }

    private fun getHorizontalScrollbarExtent(): Triple<Int, Int, Int>? {
        if (!horizontallyScrollable) {
            return null
        }

        val usedWidth = getMaxUsedWidth()
        val usableWidth = getUsableWidth()
        val offset = width - usableWidth

        if (usedWidth <= usableWidth) {
            return null
        }

        val ratio = usableWidth.toDouble() / usedWidth
        val barSize = ratio * usableWidth
        val start = (horizontalScrollPosition / (usedWidth - usableWidth)) * usableWidth * (1 - ratio)
        val end = start + barSize
        val x1 = (x + start + offset).toInt()
        val x2 = (x + end).toInt()

        return Triple(x1, x2, getHorizontalScrollbarYPosition())
    }

    private fun getVerticalScrollbarXPosition(): Int {
        return x + width - 3
    }

    private fun getHorizontalScrollbarYPosition(): Int {
        return y + height -4
    }

    private fun childVisible(translated: ChildWidget): Boolean {
        return translated.widget.visible && translated.row >= 0 && translated.row < totalRows()
    }

    private fun shouldBlockScroll(mouseX: Double, mouseY: Double): Boolean {
        return (!translucentInteract && visible && active && isMouseOver(mouseX, mouseY))
                || children.any { (_, child) ->
            child.widget is ContainerWidget && child.widget.shouldBlockScroll(mouseX, mouseY) }
    }

    private fun unfocus() {
        focusedWidget = null
        isFocused = false
        children.values.forEach {
            if (it.widget is ContainerWidget) {
                it.widget.unfocus()
            }
            else {
                it.widget.isFocused = false
            }
        }
    }

    companion object {
        private const val TOP_MARGIN = 12
        private const val X_MARGIN = 5
        private const val SCROLLBAR_GRACE = 4

        fun getRowHeight(fontHeight: Int): Double {
            return (1.35 * fontHeight)
        }

        private fun makeBackButton(backButtonCallback: () -> Unit): ClickableTextWidget {
            return backButtonCallback.let {
                ClickableTextWidget("< ${ScreenTexts.BACK.string}", onClick = { it() })
            }
        }
    }

    data class ChildWidget(
        val id: String, val widget: ClickableWidget, val row: Int, val xOffset: Int, val fromRender: Boolean = false) {
        fun translated(row: Int, xOffset: Int = 0): ChildWidget {
            return copy(row = this.row - row, xOffset = this.xOffset + xOffset)
        }
    }
}