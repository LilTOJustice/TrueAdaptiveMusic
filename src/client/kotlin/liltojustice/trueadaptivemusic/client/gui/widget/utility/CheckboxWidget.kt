package liltojustice.trueadaptivemusic.client.gui.widget.utility

import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.widget.CheckboxWidget
import net.minecraft.text.Text
import net.minecraft.util.Colors
import net.minecraft.util.Identifier
import kotlin.math.max

class CheckboxWidget(
    prompt: String,
    private val onChange: (checked: Boolean) -> Unit,
    x: Int = 0,
    y: Int = 0,
    checked: Boolean = true
):
    CheckboxWidget(
        x,
        y,
        0,
        Text.literal(prompt),
        MinecraftClient.getInstance().textRenderer,
        checked,
        { widget, checked -> }
    ) {
    val textRenderer: TextRenderer = MinecraftClient.getInstance().textRenderer

    init {
        width = CHECKBOX_SIZE + PADDING + textRenderer.getWidth(prompt)
        height = max(textRenderer.fontHeight, CHECKBOX_SIZE)
        onChange(isChecked)
    }

    override fun onPress() {
        super.onPress()
        onChange(isChecked)
    }

    override fun renderWidget(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        RenderSystem.enableDepthTest()

        context?.setShaderColor(1.0f, 1.0f, 1.0f, alpha)
        RenderSystem.enableBlend()
        context?.drawGuiTexture(
            if (isChecked) CHECKED else UNCHECKED,
            x,
            y,
            CHECKBOX_SIZE,
            CHECKBOX_SIZE,
        )
        context?.drawTextWithShadow(
            textRenderer,
            message,
            x + CHECKBOX_SIZE + PADDING,
            y,
            Colors.WHITE
        )
    }

    companion object {
        private val UNCHECKED = Identifier.ofVanilla("widget/checkbox")
        private val CHECKED = Identifier.ofVanilla("widget/checkbox_selected")
        private const val PADDING = 5
        private const val CHECKBOX_SIZE = 10
    }
}