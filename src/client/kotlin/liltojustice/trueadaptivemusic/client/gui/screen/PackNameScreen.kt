package liltojustice.trueadaptivemusic.client.gui.screen

import liltojustice.trueadaptivemusic.Constants
import liltojustice.trueadaptivemusic.client.music.MusicPack
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.EditBoxWidget
import net.minecraft.client.gui.widget.IconButtonWidget
import net.minecraft.text.Text
import net.minecraft.util.Colors
import net.minecraft.util.Identifier
import kotlin.io.path.Path
import kotlin.io.path.exists

@Environment(EnvType.CLIENT)
class PackNameScreen(private val parent: Screen): Screen(
    Text.translatableWithFallback("trueadaptivemusic.name_pack", "Name Your New Pack")) {
    private var packName = ""
    private var errorText = ""
    private lateinit var packNameWidget: EditBoxWidget
    private lateinit var acceptButtonWidget: IconButtonWidget

    override fun init() {
        packNameWidget = EditBoxWidget(
            client?.textRenderer,
            width / 2 - width / 6,
            height / 2,
            width / 3,
            (client?.textRenderer?.fontHeight ?: 0) + 5,
            Text.translatableWithFallback("trueadaptivemusic.pack_name", "Pack Name"),
            Text.translatableWithFallback("trueadaptivemusic.music_pack_name", "Music Pack Name")
        )
        packNameWidget.setChangeListener { packName ->
            errorText = ""
            this.packName = packName
            if (Path(Constants.MUSIC_PACK_DIR, "$packName.zip").exists()) {
                errorText = Text.translatableWithFallback(
                    "trueadaptivemusic.name_already_exists",
                    "%s.zip already exists",
                    packName).toString()
            }
        }
        acceptButtonWidget = IconButtonWidget.Builder(
            Text.translatableWithFallback("trueadaptivemusic.accept", "Accept"),
            CHECKMARK) {
            if (!validPackName(packName) || errorText.isNotEmpty()) {
                return@Builder
            }

            client?.setScreen(EditPackScreen(parent, MusicPack.makeEmpty(packName)))
        }
            .iconSize(9, 8)
            .textureSize(9, 8)
            .xyOffset(16, 6)
            .build()
        acceptButtonWidget.width = 60
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
        context?.drawText(
            client?.textRenderer,
            errorText,
            width / 2 - width / 6,
            height / 2 + (client?.textRenderer?.fontHeight ?: 0) + 35,
            Colors.RED,
            false)
        context?.drawCenteredTextWithShadow(
            client?.textRenderer,
            Text.translatableWithFallback("trueadaptivemusic.name_pack", "Name Your New Pack"),
            width / 2,
            10,
            Colors.WHITE)
        acceptButtonWidget.active = errorText.isEmpty() && validPackName(packName)
        super.render(context, mouseX, mouseY, delta)
    }

    companion object {
        private val CHECKMARK: Identifier = Identifier("minecraft", "textures/gui/checkmark.png")

        fun validPackName(packName: String): Boolean {
            if (packName.isEmpty()) {
                return false
            }

            try {
                Path(Constants.MUSIC_PACK_DIR, "$packName.zip")
            } catch (_: Exception) {
                return false
            }

            return true
        }
    }
}