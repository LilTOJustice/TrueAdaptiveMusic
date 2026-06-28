package liltojustice.trueadaptivemusic.client.gui.screen

import liltojustice.trueadaptivemusic.Constants
import liltojustice.trueadaptivemusic.client.music.pack.MusicPack
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.components.SpriteIconButton
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.util.CommonColors
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.pathString

@Environment(EnvType.CLIENT)
class PackNameScreen(private val parent: Screen): Screen(
    Component.translatableWithFallback("trueadaptivemusic.name_pack", "Name Your New Pack")) {
    private var packName = ""
    private var errorText = ""
    private lateinit var packNameWidget: EditBox
    private lateinit var acceptButtonWidget: SpriteIconButton

    override fun init() {
        packNameWidget = EditBox(
            font,
            width / 2 - width / 6,
            height / 2,
            width / 3,
            minecraft.font.lineHeight + 5,
            Component.translatableWithFallback("trueadaptivemusic.pack_name", "Pack Name")
        )

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

        acceptButtonWidget = SpriteIconButton.builder(
            CommonComponents.GUI_PROCEED, {
                if (!validPackName(packName) || errorText.isNotEmpty()) {
                    return@builder
                }

                minecraft.gui.setScreen(EditPackScreen(parent, MusicPack.makeEmpty(packName)))
            },
            false
        ).sprite(CHECKMARK, 9, 8).build()

        acceptButtonWidget.width = 60
        acceptButtonWidget.x = width / 2 - width / 6
        acceptButtonWidget.y = height / 2 + font.lineHeight + 10

        addRenderableWidget(packNameWidget)
        addRenderableWidget(acceptButtonWidget)
    }

    override fun onClose() {
        minecraft.gui.setScreen(parent)
    }

    override fun extractRenderState(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, a: Float) {
        graphics.text(
            font,
            errorText,
            width / 2 - width / 6,
            height / 2 + font.lineHeight + 35,
            CommonColors.RED,
            false
        )
        graphics.centeredText(
            font,
            Component.translatableWithFallback("trueadaptivemusic.name_pack", "Name Your New Pack"),
            width / 2,
            10,
            CommonColors.WHITE
        )
        acceptButtonWidget.active = errorText.isEmpty() && validPackName(packName)
        super.extractRenderState(graphics, mouseX, mouseY, a)
    }

    companion object {
        private val CHECKMARK: Identifier = Identifier.withDefaultNamespace("icon/checkmark")

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