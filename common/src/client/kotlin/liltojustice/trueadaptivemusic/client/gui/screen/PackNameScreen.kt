package liltojustice.trueadaptivemusic.client.gui.screen

import liltojustice.trueadaptivemusic.Constants
import liltojustice.trueadaptivemusic.client.music.pack.MusicPack
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.components.TextAndImageButton
import net.minecraft.client.gui.components.EditBox
import net.minecraft.network.chat.Component
import net.minecraft.util.CommonColors
import net.minecraft.resources.ResourceLocation
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.pathString

class PackNameScreen(private val parent: Screen): Screen(
    Component.translatableWithFallback("trueadaptivemusic.name_pack", "Name Your New Pack")) {
    private var packName = ""
    private var errorText = ""
    private lateinit var packNameWidget: EditBox
    private lateinit var acceptButtonWidget: TextAndImageButton

    override fun init() {
        packNameWidget = EditBox(
            font,
            width / 2 - width / 6,
            height / 2,
            width / 3,
            (minecraft?.font?.lineHeight ?: 0) + 5,
            Component.translatableWithFallback("trueadaptivemusic.pack_name", "Pack Name"))

        packNameWidget.setResponder { packName ->
            errorText = ""
            this.packName = packName
            if (Path(Constants.MUSIC_PACK_DIR.pathString, "$packName.zip").exists()) {
                errorText = Component.translatableWithFallback(
                    "trueadaptivemusic.name_already_exists",
                    "%s.zip already exists",
                    packName).string
            }
        }
        acceptButtonWidget = TextAndImageButton.Builder(
            Component.translatableWithFallback("trueadaptivemusic.accept", "Accept"),
            CHECKMARK) {
            if (!validPackName(packName) || errorText.isNotEmpty()) {
                return@Builder
            }

            minecraft?.setScreen(EditPackScreen(parent, MusicPack.makeEmpty(packName)))
        }
            .usedTextureSize(9, 8)
            .textureSize(9, 8)
            .offset(16, 6)
            .build()
        acceptButtonWidget.width = 60
        acceptButtonWidget.x = width / 2 - width / 6
        acceptButtonWidget.y = height / 2 + (minecraft?.font?.lineHeight ?: 0) + 10

        addRenderableWidget(packNameWidget)
        addRenderableWidget(acceptButtonWidget)
    }

    override fun onClose() {
        minecraft?.setScreen(parent)
    }

    override fun render(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        renderBackground(context)
        context.drawString(
            minecraft!!.font,
            errorText,
            width / 2 - width / 6,
            height / 2 + (minecraft?.font?.lineHeight ?: 0) + 35,
            CommonColors.RED,
            false)
        context.drawCenteredString(
            minecraft!!.font,
            Component.translatableWithFallback("trueadaptivemusic.name_pack", "Name Your New Pack"),
            width / 2,
            10,
            CommonColors.WHITE)
        acceptButtonWidget.active = errorText.isEmpty() && validPackName(packName)
        super.render(context, mouseX, mouseY, delta)
    }

    companion object {
        private val CHECKMARK: ResourceLocation = ResourceLocation("minecraft", "textures/gui/checkmark.png")

        fun validPackName(packName: String): Boolean {
            if (packName.isEmpty()) {
                return false
            }

            try {
                Path(Constants.MUSIC_PACK_DIR.pathString, "$packName.zip")
            } catch (_: Exception) {
                return false
            }

            return true
        }
    }
}