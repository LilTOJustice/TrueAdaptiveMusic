package liltojustice.trueadaptivemusic.client.gui.extensions

import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.text.Text
import net.minecraft.util.Colors
import net.minecraft.util.Util
import net.minecraft.util.math.MathHelper
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

fun DrawContext.drawBorder(x: Int, y: Int, width: Int, height: Int, padding: Int = 0) {
    val adjWidth = width + padding
    val adjHeight = height + padding
    val adjX = x - padding / 2
    val adjY = y - padding / 2

    this.fill(
        adjX,
        adjY,
        adjX + adjWidth,
        adjY + 1,
        Colors.WHITE
    )
    this.fill(
        adjX,
        adjY + adjHeight - 1,
        adjX + adjWidth,
        adjY + adjHeight,
        Colors.WHITE
    )
    this.fill(
        adjX,
        adjY + 1,
        adjX + 1,
        adjY + adjHeight - 1,
        Colors.WHITE
    )
    this.fill(
        adjX + adjWidth - 1,
        adjY + 1,
        adjX + adjWidth,
        adjY + adjHeight - 1,
        Colors.WHITE
    )
}

fun DrawContext.drawMarqueedWrappedText(
    font: TextRenderer, text: Text, left: Int, right: Int, top: Int, bottom: Int) {
    val width = right - left
    val lines = font.wrapLines(text, width)
    val height = lines.size * font.fontHeight
    val usableHeight = bottom - top
    val heightDiff = height - usableHeight
    if (heightDiff <= 0) {
        drawWrappedText(font, text, left, top, width, Colors.WHITE, false)

        return
    }

    val scaledTime = Util.getMeasuringTimeMs().toDouble() / 1000.0
    val clamp = max(heightDiff.toDouble() * 0.5, 3.0)
    val oscillator = sin((Math.PI / 2.0) * cos((Math.PI * 2.0) * scaledTime / clamp)) / 2.0 + 0.5
    val heightOffset = MathHelper.lerp(oscillator, 0.0, heightDiff.toDouble())
    var yOffset = 0
    enableScissor(left, top, right, bottom)
    font.wrapLines(text, width).forEach {
        val y = yOffset + top
        drawText(font, it, left, y - heightOffset.toInt(), Colors.WHITE, false)
        yOffset += font.fontHeight
    }

    disableScissor()
}