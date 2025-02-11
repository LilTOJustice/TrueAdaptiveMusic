package liltojustice.trueadaptivemusic.client.gui

import liltojustice.trueadaptivemusic.Logger
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.IconButtonWidget
import net.minecraft.text.Text
import net.minecraft.util.Identifier

@Environment(EnvType.CLIENT)
class EditPackScreen(private val parent: Screen): Screen(Text.literal("Create or edit a music pack")) {
    private lateinit var saveButtonWidget: IconButtonWidget

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
        super.render(context, mouseX, mouseY, delta)
    }

    companion object {
        private val CHECKMARK: Identifier = Identifier("minecraft", "textures/gui/checkmark.png")
    }
}