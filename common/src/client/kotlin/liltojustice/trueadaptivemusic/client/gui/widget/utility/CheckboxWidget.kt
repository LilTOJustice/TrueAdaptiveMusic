package liltojustice.trueadaptivemusic.client.gui.widget.utility

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Checkbox
import net.minecraft.client.input.InputWithModifiers
import net.minecraft.network.chat.Component
import net.minecraft.util.CommonColors
import net.minecraft.resources.Identifier
import kotlin.math.max

class CheckboxWidget(
    prompt: String,
    private val onChange: (checked: Boolean) -> Unit,
    x: Int = 0,
    y: Int = 0,
    checked: Boolean = true
):
    Checkbox(
        x,
        y,
        0,
        Component.literal(prompt),
        Minecraft.getInstance().font,
        checked,
        { _, _ -> }
    ) {
    val textRenderer: Font = Minecraft.getInstance().font

    init {
        width = CHECKBOX_SIZE + PADDING + textRenderer.width(prompt)
        height = max(textRenderer.lineHeight, CHECKBOX_SIZE)
        onChange(selected())
    }

    override fun onPress(input: InputWithModifiers) {
        super.onPress(input)
        onChange(selected())
    }

    override fun renderWidget(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        context.blitSprite(
            RenderPipelines.GUI_TEXTURED,
            if (selected()) CHECKED else UNCHECKED,
            x,
            y,
            CHECKBOX_SIZE,
            CHECKBOX_SIZE,
            CommonColors.WHITE)
        context.drawString(
            textRenderer,
            message,
            x + CHECKBOX_SIZE + PADDING,
            y,
            CommonColors.WHITE
        )
    }

    companion object {
        private val UNCHECKED = Identifier.withDefaultNamespace("widget/checkbox")
        private val CHECKED = Identifier.withDefaultNamespace("widget/checkbox_selected")
        private const val PADDING = 5
        private const val CHECKBOX_SIZE = 10
    }
}