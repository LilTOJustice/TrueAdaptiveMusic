package liltojustice.trueadaptivemusic.client.gui.screen

import liltojustice.trueadaptivemusic.Constants
import liltojustice.trueadaptivemusic.client.music.pack.MusicPack
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.gui.components.ImageButton
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.pathString

@Environment(EnvType.CLIENT)
class PackNameScreen(private val parent: Screen): Screen(
    Component.translatableWithFallback("trueadaptivemusic.name_pack", "Name Your New Pack")) {
    private var packName = ""
    private var errorText = ""
    private lateinit var packNameWidget: TextFieldWidget
    private lateinit var acceptButtonWidget: ImageButton

    override fun init() {
        packNameWidget = TextFieldWidget(
            font,
            width / 2 - width / 6,
            height / 2,
            width / 3,
            minecraft.font.lineHeight + 5,
            Component.translatableWithFallback("trueadaptivemusic.pack_name", "Pack Name")
        )

        packNameWidget.setChangedListener { packName ->
            errorText = ""
            this.packName = packName
            if (Path(Constants.MUSIC_PACK_DIR.pathString, "$packName.zip").exists()) {
                errorText = Component.translatableWithFallback(
                    "trueadaptivemusic.name_already_exists",
                    "%s.zip already exists",
                    packName).string
            }
        }
        acceptButtonWidget = ImageButton.builder(
            Component.translatableWithFallback("trueadaptivemusic.accept", "Accept")
        ) {
            if (!validPackName(packName) || errorText.isNotEmpty()) {
                return@builder
            }

            minecraft.setScreen(EditPackScreen(parent, MusicPack.makeEmpty(packName)))
        }
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
        private val CHECKMARK: Identifier = Identifier.ofVanilla("icon/checkmark")

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