package liltojustice.trueadaptivemusic.client.gui.widget

import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen.OPTIONS_BACKGROUND_TEXTURE
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.text.Text
import net.minecraft.util.Colors

abstract class ContainerWidget(width: Int, height: Int, message: Text, x: Int = 0, y: Int = 0)
    : ClickableWidget(x, y, width, height, message) {
    override fun renderButton(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        render(context, mouseX, mouseY, delta)
    }

    override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        context?.setShaderColor(0.125f, 0.125f, 0.125f, 1.0f)
        context?.drawTexture(
            OPTIONS_BACKGROUND_TEXTURE,
            x,
            y,
            0F,
            0F,
            width,
            height,
            32,
            32
        )
        context?.setShaderColor(1f, 1f, 1f, 1f)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        return clicked(mouseX, mouseY)
    }

    fun drawText(
        drawContext: DrawContext?,
        textRenderer: TextRenderer,
        text: String,
        x: Int,
        y: Int,
        color: Int = Colors.WHITE,
        shadow: Boolean = false) {
        drawContext?.drawText(textRenderer, text, x + this.x, y + this.y, color, shadow)
    }
}