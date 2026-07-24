package liltojustice.trueadaptivemusic.client.gui.widget.utility

import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Checkbox
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Mth
import kotlin.math.max

class CheckboxWidget(
    prompt: String,
    private val onChange: (checked: Boolean) -> Unit,
    x: Int = 0,
    y: Int = 0,
    checked: Boolean = true): Checkbox(x, y, 0, 0, Component.literal(prompt), checked) {
    val textRenderer: Font = Minecraft.getInstance().font

    init {
        width = CHECKBOX_SIZE + PADDING + textRenderer.width(prompt)
        height = max(textRenderer.lineHeight, CHECKBOX_SIZE)
        onChange(selected())
    }

    override fun onPress() {
        super.onPress()
        onChange(selected())
    }

    override fun renderWidget(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        RenderSystem.enableDepthTest()

        context.setColor(1.0f, 1.0f, 1.0f, alpha)
        RenderSystem.enableBlend()
        context.blit(
            TEXTURE,
            x,
            y,
            CHECKBOX_SIZE,
            CHECKBOX_SIZE,
            if (isFocused) 20.0f else 0.0f,
            if (selected()) 20.0f else 0.0f,
            20,
            20,
            64,
            64
        )
        context.setColor(1.0f, 1.0f, 1.0f, 1.0f)
        context.drawString(
            textRenderer,
            message,
            x + CHECKBOX_SIZE + PADDING,
            y,
            14737632 or (Mth.ceil(this.alpha * 255.0f) shl 24)
        )
    }

    companion object {
        private val TEXTURE = ResourceLocation("textures/gui/checkbox.png")
        private const val PADDING = 5
        private const val CHECKBOX_SIZE = 10
    }
}