package liltojustice.trueadaptivemusic.client.gui.extensions

import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import net.minecraft.util.CommonColors
import net.minecraft.Util
import net.minecraft.util.Mth
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

fun GuiGraphics.drawMarqueedWrappedText(
    textRenderer: Font, text: Component, left: Int, right: Int, top: Int, bottom: Int) {
    val width = right - left
    val lines = textRenderer.split(text, width)
    val height = lines.size * textRenderer.lineHeight
    val usableHeight = bottom - top
    val heightDiff = height - usableHeight
    if (heightDiff <= 0) {
        drawWordWrap(textRenderer, text, left, top, width, CommonColors.WHITE)

        return
    }

    val scaledTime = Util.getMillis().toDouble() / 1000.0
    val clamp = max(heightDiff.toDouble() * 0.5, 3.0)
    val oscillator = sin((Math.PI / 2.0) * cos((Math.PI * 2.0) * scaledTime / clamp)) / 2.0 + 0.5
    val heightOffset = Mth.lerp(oscillator, 0.0, heightDiff.toDouble())
    var yOffset = 0
    enableScissor(left, top, right, bottom)
    textRenderer.split(text, width).forEach {
        val y = yOffset + top
        drawString(textRenderer, it, left, y - heightOffset.toInt(), CommonColors.WHITE, false)
        yOffset += textRenderer.lineHeight
    }

    disableScissor()
}