package liltojustice.trueadaptivemusic.client.gui.screen

import liltojustice.trueadaptivemusic.Constants
import liltojustice.trueadaptivemusic.client.gui.widget.PackBrowserListWidget
import liltojustice.trueadaptivemusic.client.music.pack.BrowsableMusicPack
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.screen.ScreenTexts
import net.minecraft.text.Text
import net.minecraft.util.Colors
import net.minecraft.util.Util

@Environment(EnvType.CLIENT)
class PackBrowserScreen(private val parent: Screen): Screen(
    Text.translatableWithFallback("trueadaptivemusic.music_packs", "Music Packs")) {
    private lateinit var packListWidget: PackBrowserListWidget
    private lateinit var openMusicPacksButton: ButtonWidget
    private lateinit var doneButton: ButtonWidget
    private lateinit var refreshButton: ButtonWidget
    private var selectedPack: BrowsableMusicPack? = null

    override fun init() {
        openMusicPacksButton = ButtonWidget.Builder(OPEN_MUSIC_PACKS_TEXT) {
            Util.getOperatingSystem().open(Constants.MUSIC_PACK_DIR.toUri())
        }.build()
        openMusicPacksButton.width = textRenderer.getWidth(OPEN_MUSIC_PACKS_TEXT) + 10
        openMusicPacksButton.x = width - openMusicPacksButton.width

        packListWidget = PackBrowserListWidget(
            client!!, this.width, this.height - 96, 48, 36)
        { musicPack -> selectedPack = musicPack }

        doneButton = ButtonWidget.builder(ScreenTexts.DONE) { _: ButtonWidget? -> client?.setScreen(parent) }.build()
        doneButton.width = textRenderer.getWidth(ScreenTexts.DONE) + 10
        doneButton.x = width - doneButton.width
        doneButton.y = height - doneButton.height - 2

        refreshButton = ButtonWidget.builder(REFRESH_TEXT) { _: ButtonWidget? -> reload() }.build()
        refreshButton.y = 5
        refreshButton.width = textRenderer.getWidth(REFRESH_TEXT) + 10

        addSelectableChild(packListWidget)
        addDrawableChild(openMusicPacksButton)
        addDrawableChild(doneButton)
        addDrawableChild(refreshButton)
    }

    override fun close() {
        client?.setScreen(parent)
    }

    override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(context, mouseX, mouseY, delta)
        this.packListWidget.render(context, mouseX, mouseY, delta)
        context?.drawCenteredTextWithShadow(
            this.textRenderer, this.title, this.width / 2, 28, Colors.WHITE)
    }

    fun reload() {
        packListWidget.init()
    }

    companion object {
        private val OPEN_MUSIC_PACKS_TEXT = Text.translatableWithFallback(
            "trueadaptivemusic.open_pack_folder", "Open Pack Folder")
        private val REFRESH_TEXT = Text.translatableWithFallback("trueadaptivemusic.refresh", "Refresh")
    }
}