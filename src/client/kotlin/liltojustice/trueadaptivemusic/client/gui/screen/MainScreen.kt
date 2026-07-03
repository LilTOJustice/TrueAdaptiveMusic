package liltojustice.trueadaptivemusic.client.gui.screen

import liltojustice.trueadaptivemusic.Constants
import liltojustice.trueadaptivemusic.client.TAMClient
import liltojustice.trueadaptivemusic.client.gui.widget.PackListWidget
import liltojustice.trueadaptivemusic.client.gui.widget.utility.makeDoneButton
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.ConfirmLinkScreen
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ButtonWidget
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
    private lateinit var packBrowserButton: ButtonWidget
    private lateinit var discordButton: ButtonWidget

    override fun init() {
        createNewPackButton = ButtonWidget.Builder(CREATE_PACK_TEXT)
        {
            client?.setScreen(PackNameScreen(this))
        }.build()
        createNewPackButton.width = textRenderer.getWidth(CREATE_PACK_TEXT) + 10
        createNewPackButton.x = 1
        createNewPackButton.y = 1

        openMusicPacksButton = ButtonWidget.Builder(OPEN_MUSIC_PACKS_TEXT) {
            Util.getOperatingSystem().open(Constants.MUSIC_PACK_DIR.toUri())
        }.build()
        openMusicPacksButton.width = textRenderer.getWidth(OPEN_MUSIC_PACKS_TEXT) + 10
        openMusicPacksButton.x = width - openMusicPacksButton.width - 1
        openMusicPacksButton.y = 1

        packListWidget = PackListWidget(
            this,
            client!!,
            this.width,
            this.height,
            48,
            this.height - 64,
            36
        )
        { musicPack ->
            TAMClient.musicPack = musicPack
            editButton.visible = musicPack != null
        }

        doneButton = makeDoneButton(textRenderer, width, height) { client?.setScreen(parent) }

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
        editButton.x = 1

        refreshButton = ButtonWidget.builder(REFRESH_TEXT) { _: ButtonWidget? -> reload() }.build()
        refreshButton.y = createNewPackButton.y + createNewPackButton.height + 4
        refreshButton.width = textRenderer.getWidth(REFRESH_TEXT) + 10
        refreshButton.x = 1

        wikiButton = ButtonWidget.builder(WIKI_TEXT)
        { _: ButtonWidget? -> Util.getOperatingSystem().open(Constants.WIKI_LINK) }.build()
        wikiButton.y = openMusicPacksButton.y + openMusicPacksButton.height + 4
        wikiButton.width = textRenderer.getWidth(WIKI_TEXT) + 10
        wikiButton.x = width - wikiButton.width - 1

        optionsButton = ButtonWidget.builder(OPTIONS_TEXT)
        { _: ButtonWidget? -> client?.setScreen(OptionsScreen(this)) }.build()
        optionsButton.y = doneButton.y - doneButton.height - 3
        optionsButton.width = textRenderer.getWidth(OPTIONS_TEXT) + 10
        optionsButton.x = width - optionsButton.width - 1

        packBrowserButton = ButtonWidget.builder(PACK_BROWSER_TEXT)
        { _: ButtonWidget? -> client?.setScreen(PackBrowserScreen(this)) }.build()
        packBrowserButton.width = textRenderer.getWidth(PACK_BROWSER_TEXT) + 10
        packBrowserButton.y = packListWidget.getBottom() + ((height - packListWidget.getBottom()) - packBrowserButton.height) / 2
        packBrowserButton.x = (this.width - packBrowserButton.width) / 2

        discordButton = ButtonWidget.builder(Constants.DISCORD_JOIN_TEXT)
        { _: ButtonWidget? -> client?.setScreen(
            ConfirmLinkScreen(
                { confirmed ->
                    if (confirmed) {
                        Util.getOperatingSystem().open(Constants.DISCORD_JOIN_URL)
                    }

                    client?.setScreen(this)
                },
                Constants.DISCORD_JOIN_URL,
                true
            )
        ) }.build()
        discordButton.width = textRenderer.getWidth(Constants.DISCORD_JOIN_TEXT) + 10
        discordButton.y = wikiButton.y
        discordButton.x = wikiButton.x - discordButton.width - 5

        packBrowserButton = ButtonWidget.builder(PACK_BROWSER_TEXT)
        { _: ButtonWidget? ->
            client?.setScreen(
                if (TAMClient.extensions != null)
                    PackBrowserScreen(this)
                else
                    ExtensionsSuggestionScreen(this)
            )
        }.build()
        packBrowserButton.width = textRenderer.getWidth(PACK_BROWSER_TEXT) + 10
        packBrowserButton.y = packListWidget.getBottom() + ((height - packListWidget.getBottom()) - packBrowserButton.height) / 2
        packBrowserButton.x = (this.width - packBrowserButton.width) / 2
        addDrawableChild(packBrowserButton)

        addSelectableChild(packListWidget)
        addDrawableChild(createNewPackButton)
        addDrawableChild(openMusicPacksButton)
        addDrawableChild(doneButton)
        addDrawableChild(editButton)
        addDrawableChild(refreshButton)
        addDrawableChild(wikiButton)
        addDrawableChild(optionsButton)
        addDrawableChild(packBrowserButton)
        addDrawableChild(discordButton)
    }

    override fun close() {
        client?.setScreen(parent)
    }

    override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        this.renderBackground(context)
        this.packListWidget.render(context, mouseX, mouseY, delta)
        context?.drawCenteredTextWithShadow(
            this.textRenderer, this.title, this.width / 2, 28, Colors.WHITE)
        super.render(context, mouseX, mouseY, delta)
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
        private val PACK_BROWSER_TEXT = Text.translatableWithFallback(
            "trueadaptivemusic.open_pack_browser", "Get More Packs")
    }
}