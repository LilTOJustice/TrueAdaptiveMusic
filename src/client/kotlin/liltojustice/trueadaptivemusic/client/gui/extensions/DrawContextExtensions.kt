package liltojustice.trueadaptivemusic.client.gui.extensions

import net.minecraft.client.gui.DrawContext
import net.minecraft.util.Colors

fun DrawContext.drawBorder(x: Int, y: Int, width: Int, height: Int, padding: Int = 0) {
    val adjWidth = width + padding
    val adjHeight = height + padding
    val adjX = x - padding / 2
    val adjY = y - padding / 2

    this.fill(adjX, adjY, adjX + adjWidth, adjY + 1, Colors.WHITE)
    this.fill(adjX, adjY + adjHeight - 1, adjX + adjWidth, adjY + adjHeight, Colors.WHITE)
    this.fill(adjX, adjY + 1, adjX + 1, adjY + adjHeight - 1, Colors.WHITE)
    this.fill(adjX + adjWidth - 1, adjY + 1, adjX + adjWidth, adjY + adjHeight - 1, Colors.WHITE)
}