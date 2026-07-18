package liltojustice.trueadaptivemusic.client.gui.widget.utility

import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import net.minecraft.Util
import net.minecraft.util.Mth
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

fun drawScrollableText(
    context: GuiGraphics,
    textRenderer: Font,
    text: Component,
    left: Int,
    top: Int,
    right: Int,
    bottom: Int,
    color: Int
) {
    val i = textRenderer.width(text)
    val var10000 = top + bottom
    val j = (var10000 - 9) / 2 + 1
    val k = right - left
    if (i > k) {
        val l = i - k
        val d = Util.getMillis().toDouble() / 1000.0
        val e = max(l.toDouble() * 0.5, 3.0)
        val f = sin((Math.PI / 2.0) * cos((Math.PI * 2.0) * d / e)) / 2.0 + 0.5
        val g = Mth.lerp(f, 0.0, l.toDouble())
        context.enableScissor(left, top, right, bottom)
        context.drawString(textRenderer, text, left - g.toInt(), j, color)
        context.disableScissor()
    } else {
        context.drawString(textRenderer, text, left, j, color)
    }
}