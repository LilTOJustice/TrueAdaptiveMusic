package liltojustice.trueadaptivemusic.client.gui.extensions

import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import net.minecraft.util.CommonColors
import net.minecraft.util.Util
import net.minecraft.util.Mth
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

fun GuiGraphics.drawBorder(x: Int, y: Int, width: Int, height: Int, padding: Int = 0) {
    val adjWidth = width + padding
    val adjHeight = height + padding
    val adjX = x - padding / 2
    val adjY = y - padding / 2

    this.fill(
        adjX,
        adjY,
        adjX + adjWidth,
        adjY + 1,
        CommonColors.WHITE
    )
    this.fill(
        adjX,
        adjY + adjHeight - 1,
        adjX + adjWidth,
        adjY + adjHeight,
        CommonColors.WHITE
    )
    this.fill(
        adjX,
        adjY + 1,
        adjX + 1,
        adjY + adjHeight - 1,
        CommonColors.WHITE
    )
    this.fill(
        adjX + adjWidth - 1,
        adjY + 1,
        adjX + adjWidth,
        adjY + adjHeight - 1,
        CommonColors.WHITE
    )
}

fun GuiGraphics.drawMarqueedWrappedText(
    font: Font, text: Component, left: Int, right: Int, top: Int, bottom: Int) {
    val width = right - left
    val lines = font.split(text, width)
    val height = lines.size * font.lineHeight
    val usableHeight = bottom - top
    val heightDiff = height - usableHeight
    if (heightDiff <= 0) {
        drawWordWrap(font, text, left, top, width, CommonColors.WHITE, false)

        return
    }

    val scaledTime = Util.getMillis().toDouble() / 1000.0
    val clamp = max(heightDiff.toDouble() * 0.5, 3.0)
    val oscillator = sin((Math.PI / 2.0) * cos((Math.PI * 2.0) * scaledTime / clamp)) / 2.0 + 0.5
    val heightOffset = Mth.lerp(oscillator, 0.0, heightDiff.toDouble())
    var yOffset = 0
    enableScissor(left, top, right, bottom)
    font.split(text, width).forEach {
        val y = yOffset + top
        drawString(font, it, left, y - heightOffset.toInt(), CommonColors.WHITE, false)
        yOffset += font.lineHeight
    }

    disableScissor()
}