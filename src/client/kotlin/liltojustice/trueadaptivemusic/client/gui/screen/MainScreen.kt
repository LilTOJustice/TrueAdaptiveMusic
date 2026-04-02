package liltojustice.trueadaptivemusic.client.gui.screen

import liltojustice.trueadaptivemusic.Constants
import liltojustice.trueadaptivemusic.client.TAMClient
import liltojustice.trueadaptivemusic.client.gui.widget.PackListWidget
import liltojustice.trueadaptivemusic.client.gui.widget.utility.makeDoneButton
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.screens.ConfirmLinkScreen
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import net.minecraft.util.CommonColors
import net.minecraft.util.Util
import java.nio.file.Path
import kotlin.io.path.*

@Environment(EnvType.CLIENT)
class MainScreen(private val parent: Screen): Screen(
    Component.translatableWithFallback("trueadaptivemusic.music_packs", "Music Packs")) {
    private lateinit var createNewPackButton: Button
    private lateinit var packListWidget: PackListWidget
    private lateinit var openMusicPacksButton: Button
    private lateinit var doneButton: Button
    private lateinit var editButton: Button
    private lateinit var refreshButton: Button
    private lateinit var wikiButton: Button
    private lateinit var optionsButton: Button
    private lateinit var packBrowserButton: Button
    private lateinit var discordButton: Button

    override fun init() {
        createNewPackButton = Button.Builder(CREATE_PACK_TEXT)
        {
            minecraft.setScreen(PackNameScreen(this))
        }.build()
        createNewPackButton.width = font.width(CREATE_PACK_TEXT) + 10
        createNewPackButton.x = 1
        createNewPackButton.y = 1

        openMusicPacksButton = Button.Builder(OPEN_MUSIC_PACKS_TEXT) {
            Util.getPlatform().openUri(Constants.MUSIC_PACK_DIR.toUri())
        }.build()
        openMusicPacksButton.width = font.width(OPEN_MUSIC_PACKS_TEXT) + 10
        openMusicPacksButton.x = width - openMusicPacksButton.width - 1
        openMusicPacksButton.y = 1

        packListWidget = PackListWidget(
            this, minecraft, this.width, this.height - 96, 48, 36)
        { musicPack ->
            TAMClient.musicPack = musicPack
            editButton.visible = musicPack != null
        }

        doneButton = makeDoneButton(font, width, height) { minecraft.setScreen(parent) }

        editButton = Button.Builder(EDIT_TEXT)
        {
            val currentPack = TAMClient.musicPack!!
            val ongoingEdit = getOngoingEdit(Path(currentPack.packName))
            val editScreen = EditPackScreen(this, currentPack)
            if (ongoingEdit != null && ongoingEdit.name != currentPack.packName) {
                minecraft.setScreen(
                    ConfirmBackupScreen(this, ongoingEdit, editScreen))

                return@Builder
            }

            minecraft.setScreen(editScreen)
        }.build()
        editButton.width = font.width(EDIT_TEXT) + 10
        editButton.y = height - editButton.height - 2
        editButton.visible = TAMClient.musicPack != null
        editButton.x = 1

        refreshButton = Button.builder(REFRESH_TEXT) { _: Button? -> reload() }.build()
        refreshButton.y = createNewPackButton.y + createNewPackButton.height + 4
        refreshButton.width = font.width(REFRESH_TEXT) + 10
        refreshButton.x = 1

        wikiButton = Button.builder(WIKI_TEXT)
        { _: Button? -> Util.getPlatform().openUri(Constants.WIKI_LINK) }.build()
        wikiButton.y = openMusicPacksButton.y + openMusicPacksButton.height + 4
        wikiButton.width = font.width(WIKI_TEXT) + 10
        wikiButton.x = width - wikiButton.width - 1

        optionsButton = Button.builder(OPTIONS_TEXT)
        { _: Button? -> minecraft.setScreen(OptionsScreen(this)) }.build()
        optionsButton.y = doneButton.y - doneButton.height - 3
        optionsButton.width = font.width(OPTIONS_TEXT) + 10
        optionsButton.x = width - optionsButton.width - 1

        packBrowserButton = Button.builder(PACK_BROWSER_TEXT)
        { _: Button? -> minecraft.setScreen(PackBrowserScreen(this)) }.build()
        packBrowserButton.width = font.width(PACK_BROWSER_TEXT) + 10
        packBrowserButton.y = packListWidget.bottom + ((height - packListWidget.bottom) - packBrowserButton.height) / 2
        packBrowserButton.x = (this.width - packBrowserButton.width) / 2

        discordButton = Button.builder(Constants.DISCORD_JOIN_TEXT)
        { _: Button? -> minecraft.setScreen(
            ConfirmLinkScreen(
                { confirmed ->
                    if (confirmed) {
                        Util.getPlatform().openUri(Constants.DISCORD_JOIN_URL)
                    }

                    minecraft.setScreen(this)
                },
                Constants.DISCORD_JOIN_URL,
                true
            )
        ) }.build()
        discordButton.width = font.width(Constants.DISCORD_JOIN_TEXT) + 10
        discordButton.y = wikiButton.y
        discordButton.x = wikiButton.x - discordButton.width - 5


        addWidget(packListWidget)
        addWidget(createNewPackButton)
        addWidget(openMusicPacksButton)
        addWidget(doneButton)
        addWidget(editButton)
        addWidget(refreshButton)
        addWidget(wikiButton)
        addWidget(optionsButton)
        addWidget(packBrowserButton)
        addWidget(discordButton)
    }

    override fun onClose() {
        minecraft.setScreen(parent)
    }

    override fun extractRenderState(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, a: Float) {
        super.extractRenderState(graphics, mouseX, mouseY, a)
        packListWidget.extractRenderState(graphics, mouseX, mouseY, a)
        graphics.centeredText(font, this.title, this.width / 2, 28, CommonColors.WHITE)
    }

    fun reload() {
        packListWidget.init()
    }

    companion object {
        fun getOngoingEdit(packName: Path): Path? {
            return Constants.MUSIC_PACK_DIR.listDirectoryEntries().firstOrNull() { file ->
                packName.nameWithoutExtension == file.nameWithoutExtension && file.extension == "new" }
        }

        private val OPEN_MUSIC_PACKS_TEXT = Component.translatableWithFallback(
            "trueadaptivemusic.open_pack_folder", "Open Pack Folder")
        private val CREATE_PACK_TEXT = Component.translatableWithFallback(
            "trueadaptivemusic.create_pack", "Create a new music pack")
        private val REFRESH_TEXT = Component.translatableWithFallback(
            "trueadaptivemusic.refresh", "Refresh")
        private val EDIT_TEXT = Component.translatableWithFallback(
            "trueadaptivemusic.edit_pack", "Edit Pack")
        private val WIKI_TEXT = Component.translatableWithFallback(
            "trueadaptivemusic.open_wiki", "Open Wiki")
        private val OPTIONS_TEXT = Component.translatableWithFallback(
            "trueadaptivemusic.options", "Options")
        private val PACK_BROWSER_TEXT = Component.translatableWithFallback(
            "trueadaptivemusic.open_pack_browser", "Get More Packs")
    }
}