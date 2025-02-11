package liltojustice.trueadaptivemusic.client.gui.screen

import liltojustice.trueadaptivemusic.Logger
import liltojustice.trueadaptivemusic.client.gui.widget.PackStructureWidget
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.IconButtonWidget
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
        packStructureWidget = PackStructureWidget(0, 0, 500, 500)

        addDrawableChild(saveButtonWidget)
        addDrawableChild(packStructureWidget)
    }

    override fun close() {
        client?.setScreen(parent)
    }

    override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        renderBackground(context)
        context?.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 12, 16777215)
    }

    companion object {
        private val CHECKMARK: Identifier = Identifier("minecraft", "textures/gui/checkmark.png")
    }
}