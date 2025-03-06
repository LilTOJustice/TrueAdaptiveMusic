package liltojustice.trueadaptivemusic.client.gui.screen

import liltojustice.trueadaptivemusic.Constants
import liltojustice.trueadaptivemusic.client.gui.widget.PackListWidget
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.ScreenTexts
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.Text
import net.minecraft.util.Util
import kotlin.io.path.Path

@Environment(EnvType.CLIENT)
class MainScreen(private val parent: Screen): Screen(Text.of("Music Packs")) {
    private lateinit var packListWidget: PackListWidget
    private lateinit var openMusicPacksButton: ButtonWidget
    private lateinit var doneButton: ButtonWidget
    private lateinit var refreshButton: ButtonWidget
    private lateinit var wikiButton: ButtonWidget

    override fun init() {
        openMusicPacksButton = ButtonWidget(0, 0, 0, 20, OPEN_MUSIC_PACKS_TEXT) {
            Util.getOperatingSystem().open(Path(Constants.MUSIC_PACK_DIR).toUri())
        }
        openMusicPacksButton.width = textRenderer.getWidth(OPEN_MUSIC_PACKS_TEXT) + 10
        openMusicPacksButton.x = width - openMusicPacksButton.width

        packListWidget = PackListWidget(
            client!!, this.width, this.height, 48, this.height - 64, 36)

        doneButton = ButtonWidget(
            0, 0, 0, 20, ScreenTexts.DONE) { _: ButtonWidget? -> client?.setScreen(parent) }
        doneButton.width = textRenderer.getWidth(ScreenTexts.DONE) + 10
        doneButton.x = width - doneButton.width
        doneButton.y = height - doneButton.height

        refreshButton = ButtonWidget(0, 0, 0, 20, REFRESH_TEXT) { _: ButtonWidget? -> reload() }
        refreshButton.width = textRenderer.getWidth(REFRESH_TEXT) + 10

        wikiButton = ButtonWidget(0, 0, 0, 20, WIKI_TEXT)
        { _: ButtonWidget? -> Util.getOperatingSystem().open(Constants.WIKI_LINK) }
        wikiButton.y = openMusicPacksButton.y + openMusicPacksButton.height + 5
        wikiButton.width = textRenderer.getWidth(WIKI_TEXT) + 10
        wikiButton.x = width - wikiButton.width

        addSelectableChild(packListWidget)
        addDrawableChild(openMusicPacksButton)
        addDrawableChild(doneButton)
        addDrawableChild(refreshButton)
        addDrawableChild(wikiButton)
    }

    override fun close() {
        client?.setScreen(parent)
    }

    override fun render(context: MatrixStack?, mouseX: Int, mouseY: Int, delta: Float) {
        this.packListWidget.render(context, mouseX, mouseY, delta)
        drawCenteredTextWithShadow(
            context, this.textRenderer, this.title.asOrderedText(), this.width / 2, 8, 0xffffff)
        super.render(context, mouseX, mouseY, delta)
    }

    private fun reload() {
        packListWidget.init()
    }

    companion object {
        private val OPEN_MUSIC_PACKS_TEXT = Text.of("Open Pack Folder")
        private val REFRESH_TEXT = Text.of("Refresh")
        private val WIKI_TEXT = Text.of("Open Wiki")
    }
}