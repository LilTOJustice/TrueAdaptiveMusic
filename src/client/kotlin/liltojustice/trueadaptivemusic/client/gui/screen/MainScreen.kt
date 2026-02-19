package liltojustice.trueadaptivemusic.client.gui.screen

import liltojustice.trueadaptivemusic.Constants
import liltojustice.trueadaptivemusic.client.TAMClient
import liltojustice.trueadaptivemusic.client.gui.widget.PackListWidget
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.screen.ScreenTexts
import net.minecraft.text.Text
import net.minecraft.util.Colors
import net.minecraft.util.Util
import java.nio.file.Path
import kotlin.io.path.*

@Environment(EnvType.CLIENT)
class MainScreen(private val parent: Screen): Screen(
    Text.translatableWithFallback("trueadaptivemusic.music_packs", "Music Packs")) {
    private lateinit var createNewPackButton: ButtonWidget
    private lateinit var packListWidget: PackListWidget
    private lateinit var openMusicPacksButton: ButtonWidget
    private lateinit var doneButton: ButtonWidget
    private lateinit var editButton: ButtonWidget
    private lateinit var refreshButton: ButtonWidget
    private lateinit var wikiButton: ButtonWidget
    private lateinit var optionsButton: ButtonWidget
    private lateinit var ffmpegInstallButton: ButtonWidget

    override fun init() {
        createNewPackButton = ButtonWidget.Builder(CREATE_PACK_TEXT)
        {
            client?.setScreen(PackNameScreen(this))
        }.build()
        createNewPackButton.width = textRenderer.getWidth(CREATE_PACK_TEXT) + 10

        openMusicPacksButton = ButtonWidget.Builder(OPEN_MUSIC_PACKS_TEXT) {
            Util.getOperatingSystem().open(Constants.MUSIC_PACK_DIR.toUri())
        }.build()
        openMusicPacksButton.width = textRenderer.getWidth(OPEN_MUSIC_PACKS_TEXT) + 10
        openMusicPacksButton.x = width - openMusicPacksButton.width

        packListWidget = PackListWidget(client!!, this.width, this.height - 96, 48, 36)
        { musicPack ->
            TAMClient.musicPack = musicPack
            editButton.visible = musicPack != null
        }

        doneButton = ButtonWidget.builder(ScreenTexts.DONE) { _: ButtonWidget? -> client?.setScreen(parent) }.build()
        doneButton.width = textRenderer.getWidth(ScreenTexts.DONE) + 10
        doneButton.x = width - doneButton.width
        doneButton.y = height - doneButton.height - 2

        editButton = ButtonWidget.Builder(EDIT_TEXT)
        {
            val currentPack = TAMClient.musicPack!!
            val ongoingEdit = getOngoingEdit(Path(currentPack.packName))
            val editScreen = EditPackScreen(this, currentPack)
            if (ongoingEdit != null && ongoingEdit.name != currentPack.packName) {
                client?.setScreen(ConfirmBackupScreen(this, ongoingEdit, editScreen))
                return@Builder
            }

            client?.setScreen(editScreen)
        }.build()
        editButton.width = textRenderer.getWidth(EDIT_TEXT) + 10
        editButton.y = height - editButton.height - 2
        editButton.visible = TAMClient.musicPack != null

        refreshButton = ButtonWidget.builder(REFRESH_TEXT) { _: ButtonWidget? -> reload() }.build()
        refreshButton.y = createNewPackButton.y + createNewPackButton.height + 5
        refreshButton.width = textRenderer.getWidth(REFRESH_TEXT) + 10

        wikiButton = ButtonWidget.builder(WIKI_TEXT)
        { _: ButtonWidget? -> Util.getOperatingSystem().open(Constants.WIKI_LINK) }.build()
        wikiButton.y = openMusicPacksButton.y + openMusicPacksButton.height + 5
        wikiButton.width = textRenderer.getWidth(WIKI_TEXT) + 10
        wikiButton.x = width - wikiButton.width

        optionsButton = ButtonWidget.builder(OPTIONS_TEXT)
        { _: ButtonWidget? -> client?.setScreen(OptionsScreen(this)) }.build()
        optionsButton.y = doneButton.y - doneButton.height - 3
        optionsButton.width = textRenderer.getWidth(OPTIONS_TEXT) + 10
        optionsButton.x = width - optionsButton.width

        ffmpegInstallButton = ButtonWidget.builder(INSTALL_FFMPEG_TEXT)
        { _: ButtonWidget? -> client?.setScreen(ConfirmFFmpegInstallScreen(this)) }.build()
        ffmpegInstallButton.y = wikiButton.y
        ffmpegInstallButton.width = textRenderer.getWidth(INSTALL_FFMPEG_TEXT) + 10
        ffmpegInstallButton.x = wikiButton.x - ffmpegInstallButton.width - 5

        addSelectableChild(packListWidget)
        addDrawableChild(createNewPackButton)
        addDrawableChild(openMusicPacksButton)
        addDrawableChild(doneButton)
        addDrawableChild(editButton)
        addDrawableChild(refreshButton)
        addDrawableChild(wikiButton)
        addDrawableChild(optionsButton)

        if (!TAMClient.hasFFmpeg) {
            addDrawableChild(ffmpegInstallButton)
        }
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
        fun getOngoingEdit(packName: Path): Path? {
            return Constants.MUSIC_PACK_DIR.listDirectoryEntries().firstOrNull() { file ->
                packName.nameWithoutExtension == file.nameWithoutExtension && file.extension == "new" }
        }

        private val OPEN_MUSIC_PACKS_TEXT = Text.translatableWithFallback(
            "trueadaptivemusic.open_pack_folder", "Open Pack Folder")
        private val CREATE_PACK_TEXT = Text.translatableWithFallback(
            "trueadaptivemusic.create_pack", "Create a new music pack")
        private val REFRESH_TEXT = Text.translatableWithFallback("trueadaptivemusic.refresh", "Refresh")
        private val EDIT_TEXT = Text.translatableWithFallback("trueadaptivemusic.edit_pack", "Edit Pack")
        private val WIKI_TEXT = Text.translatableWithFallback("trueadaptivemusic.open_wiki", "Open Wiki")
        private val OPTIONS_TEXT = Text.translatableWithFallback("trueadaptivemusic.options", "Options")
        private val INSTALL_FFMPEG_TEXT = Text.translatableWithFallback(
            "trueadaptivemusic.ffmpeg_install", "Install FFmpeg")
    }
}