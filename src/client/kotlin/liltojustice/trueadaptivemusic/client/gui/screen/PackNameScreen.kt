package liltojustice.trueadaptivemusic.client.gui.screen

import liltojustice.trueadaptivemusic.Constants
import liltojustice.trueadaptivemusic.client.music.pack.MusicPack
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.client.gui.widget.TextIconButtonWidget
import net.minecraft.text.Text
import net.minecraft.util.Colors
import net.minecraft.util.Identifier
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.pathString

@Environment(EnvType.CLIENT)
class PackNameScreen(private val parent: Screen): Screen(
    Text.translatableWithFallback("trueadaptivemusic.name_pack", "Name Your New Pack")) {
    private var packName = ""
    private var errorText = ""
    private lateinit var packNameWidget: TextFieldWidget
    private lateinit var acceptButtonWidget: TextIconButtonWidget

    override fun init() {
        packNameWidget = TextFieldWidget(
            textRenderer,
            width / 2 - width / 6,
            height / 2,
            width / 3,
            (client?.textRenderer?.fontHeight ?: 0) + 5,
            Text.translatableWithFallback("trueadaptivemusic.pack_name", "Pack Name"))

        packNameWidget.setChangedListener { packName ->
            errorText = ""
            this.packName = packName
            if (Path(Constants.MUSIC_PACK_DIR.pathString, "$packName.zip").exists()) {
                errorText = Text.translatableWithFallback(
                    "trueadaptivemusic.name_already_exists",
                    "%s.zip already exists",
                    packName).string
            }
        }
        acceptButtonWidget = TextIconButtonWidget.Builder(
            Text.translatableWithFallback("trueadaptivemusic.accept", "Accept"),
            {
            if (!validPackName(packName) || errorText.isNotEmpty()) {
                return@Builder
            }

            client?.setScreen(EditPackScreen(parent, MusicPack.makeEmpty(packName)))
        }, false)
            .texture(CHECKMARK, 9, 8)
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
        renderBackground(context, mouseX, mouseY, delta)
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