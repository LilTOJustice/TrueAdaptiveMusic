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

fun DrawContext.drawMarqueedWrappedText(
    textRenderer: TextRenderer, text: Text, left: Int, right: Int, top: Int, bottom: Int) {
    val width = right - left
    val lines = textRenderer.wrapLines(text, width)
    val height = lines.size * textRenderer.fontHeight
    val usableHeight = bottom - top
    val heightDiff = height - usableHeight
    if (heightDiff <= 0) {
        drawWrappedText(textRenderer, text, left, top, width, Colors.WHITE, false)

        return
    }

    val scaledTime = Util.getMeasuringTimeMs().toDouble() / 1000.0
    val clamp = max(heightDiff.toDouble() * 0.5, 3.0)
    val oscillator = sin((Math.PI / 2.0) * cos((Math.PI * 2.0) * scaledTime / clamp)) / 2.0 + 0.5
    val heightOffset = MathHelper.lerp(oscillator, 0.0, heightDiff.toDouble())
    var yOffset = 0
    enableScissor(left, top, right, bottom)
    textRenderer.wrapLines(text, width).forEach {
        val y = yOffset + top
        drawText(textRenderer, it, left, y - heightOffset.toInt(), Colors.WHITE, false)
        yOffset += textRenderer.fontHeight
    }

    disableScissor()
}