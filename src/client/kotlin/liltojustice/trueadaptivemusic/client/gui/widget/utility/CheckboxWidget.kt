package liltojustice.trueadaptivemusic.client.gui.widget.utility

import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.widget.CheckboxWidget
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.MathHelper
import kotlin.math.max

class CheckboxWidget(
    private val checkboxSize: Int,
    prompt: String,
    private val onChange: (checked: Boolean) -> Unit,
    x: Int = 0,
    y: Int = 0,
    checked: Boolean = true): CheckboxWidget(x, y, 0, 0, Text.literal(prompt), checked) {
    val textRenderer: TextRenderer = MinecraftClient.getInstance().textRenderer

    init {
        width = checkboxSize + PADDING + textRenderer.getWidth(prompt)
        height = max(textRenderer.fontHeight, checkboxSize)
        onChange(isChecked)
    }

    override fun onPress() {
        super.onPress()
        onChange(isChecked)
    }

    override fun renderButton(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        RenderSystem.enableDepthTest()

        context?.setShaderColor(1.0f, 1.0f, 1.0f, alpha)
        RenderSystem.enableBlend()
        context?.drawTexture(
            TEXTURE,
            x,
            y,
            checkboxSize,
            checkboxSize,
            if (isFocused) 20.0f else 0.0f,
            if (isChecked) 20.0f else 0.0f,
            20,
            20,
            64,
            64
        )
        context?.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f)
        context?.drawTextWithShadow(
            textRenderer,
            message,
            x + checkboxSize + PADDING,
            y,
            14737632 or (MathHelper.ceil(this.alpha * 255.0f) shl 24)
        )
    }

    companion object {
        private val TEXTURE = Identifier("textures/gui/checkbox.png")
        private const val PADDING = 5
    }
}