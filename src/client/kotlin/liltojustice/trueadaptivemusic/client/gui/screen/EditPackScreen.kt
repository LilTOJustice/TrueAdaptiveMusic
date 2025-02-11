package liltojustice.trueadaptivemusic.client.gui.screen

import liltojustice.trueadaptivemusic.Logger
import liltojustice.trueadaptivemusic.client.gui.widget.PackStructureWidget
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.IconButtonWidget
import net.minecraft.client.render.RenderLayer
import net.minecraft.text.Text
import net.minecraft.util.Identifier

@Environment(EnvType.CLIENT)
class EditPackScreen(private val parent: Screen): Screen(Text.literal("Create/Edit a music pack")) {
    private lateinit var saveButtonWidget: IconButtonWidget
    private lateinit var packStructureWidget: PackStructureWidget

    override fun init() {
        saveButtonWidget = IconButtonWidget.Builder(Text.literal("Save"), CHECKMARK)
        { Logger.log("Save pack clicked") }
            .iconSize(9, 8)
            .textureSize(9, 8)
            .xyOffset(13, 6)
            .build()

        addDrawableChild(saveButtonWidget)
    }

    override fun close() {
        client?.setScreen(parent)
    }

    override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        renderBackground(context)
        context?.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 12, 16777215)
        context?.setShaderColor(0.125f, 0.125f, 0.125f, 1.0f)
        context?.drawTexture(
            OPTIONS_BACKGROUND_TEXTURE,
            0,
            TOP_MARGIN,
            0F,
            0F, // (this.bottom + this.getScrollAmount().toInt()).toFloat(),
            this.width,
            (this.height - BOTTOM_MARGIN) - TOP_MARGIN,
            32,
            32
        )
        context?.fillGradient(
            RenderLayer.getGuiOverlay(),
            0,
            TOP_MARGIN,
            this.width,
            TOP_MARGIN + 4, -16777216, 0, 0
        )
        context?.fillGradient(
            RenderLayer.getGuiOverlay(),
            0,
            this.height - BOTTOM_MARGIN - 4,
            this.width,
            this.height - BOTTOM_MARGIN, 0, -16777216, 0
        )
        super.render(context, mouseX, mouseY, delta)
    }

    companion object {
        private val CHECKMARK: Identifier = Identifier("minecraft", "textures/gui/checkmark.png")
        private const val TOP_MARGIN = 24
        private const val BOTTOM_MARGIN = 24
    }
}