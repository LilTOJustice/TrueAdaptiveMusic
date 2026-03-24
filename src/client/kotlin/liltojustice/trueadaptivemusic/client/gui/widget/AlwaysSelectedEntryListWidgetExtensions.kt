package liltojustice.trueadaptivemusic.client.gui.widget

import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.text.Text
import net.minecraft.util.Util
import net.minecraft.util.math.MathHelper
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

fun drawScrollableText(
    context: DrawContext,
    textRenderer: TextRenderer,
    text: Text?,
    left: Int,
    top: Int,
    right: Int,
    bottom: Int,
    color: Int
) {
    val i = textRenderer.getWidth(text)
    val var10000 = top + bottom
    val j = (var10000 - 9) / 2 + 1
    val k = right - left
    if (i > k) {
        val l = i - k
        val d = Util.getMeasuringTimeMs().toDouble() / 1000.0
        val e = max(l.toDouble() * 0.5, 3.0)
        val f = sin((Math.PI / 2.0) * cos((Math.PI * 2.0) * d / e)) / 2.0 + 0.5
        val g = MathHelper.lerp(f, 0.0, l.toDouble())
        context.enableScissor(left, top, right, bottom)
        context.drawTextWithShadow(textRenderer, text, left - g.toInt(), j, color)
        context.disableScissor()
    } else {
        context.drawTextWithShadow(textRenderer, text, left, j, color)
    }
}