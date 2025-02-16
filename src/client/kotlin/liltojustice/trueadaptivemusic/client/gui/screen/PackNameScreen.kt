package liltojustice.trueadaptivemusic.client.gui.screen

import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.EditBoxWidget
import net.minecraft.client.gui.widget.IconButtonWidget
import net.minecraft.text.Text
import net.minecraft.util.Identifier

@Environment(EnvType.CLIENT)
class PackNameScreen(private val parent: Screen): Screen(Text.literal("Name Your New Pack")) {
    private var packName = ""

    override fun init() {
        val packNameWidget = EditBoxWidget(
            client?.textRenderer,
            width / 2 - width / 6,
            height / 2,
            width / 3,
            (client?.textRenderer?.fontHeight ?: 0) + 5,
            Text.literal("Pack Name"),
            Text.literal("Music Pack Name")
        )
        packNameWidget.setChangeListener { packName ->
            this.packName = packName
        }
        val acceptButtonWidget = IconButtonWidget.Builder(Text.literal("Accept"), CHECKMARK) {
            if (packName.isBlank()) {
                return@Builder
            }

            client?.setScreen(EditPackScreen(parent, packName = packName))
        }
            .iconSize(9, 8)
            .textureSize(9, 8)
            .xyOffset(13, 6)
            .build()
        acceptButtonWidget.width = 50
        acceptButtonWidget.x = width / 2 - width / 6
        acceptButtonWidget.y = height / 2 + (client?.textRenderer?.fontHeight ?: 0) + 10

        addDrawableChild(packNameWidget)
        addDrawableChild(acceptButtonWidget)
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