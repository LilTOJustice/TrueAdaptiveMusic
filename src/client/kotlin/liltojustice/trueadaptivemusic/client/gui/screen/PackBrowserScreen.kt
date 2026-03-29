package liltojustice.trueadaptivemusic.client.gui.screen

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import liltojustice.trueadaptivemusic.Constants
import liltojustice.trueadaptivemusic.client.gui.widget.PackBrowserListWidget
import liltojustice.trueadaptivemusic.client.gui.widget.utility.makeDoneButton
import liltojustice.trueadaptivemusic.client.music.pack.browsable.BrowsableMusicPack
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.ConfirmLinkScreen
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.TextWidget
import net.minecraft.text.MutableText
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Colors
import net.minecraft.util.Util

@Environment(EnvType.CLIENT)
class PackBrowserScreen(private val parent: Screen): Screen(
    Text.translatableWithFallback("trueadaptivemusic.music_pack_browser", "Music Pack Browser")) {
    private lateinit var packListWidget: PackBrowserListWidget
    private lateinit var openMusicPacksButton: ButtonWidget
    private lateinit var doneButton: ButtonWidget
    private lateinit var refreshButton: ButtonWidget
    private lateinit var discordButton: ButtonWidget
    private lateinit var lastRefreshedWidget: TextWidget
    private var selectedPack: BrowsableMusicPack? = null

    override fun init() {
        openMusicPacksButton = ButtonWidget.Builder(OPEN_MUSIC_PACKS_TEXT) {
            Util.getOperatingSystem().open(Constants.MUSIC_PACK_DIR.toUri())
        }.build()
        openMusicPacksButton.width = textRenderer.getWidth(OPEN_MUSIC_PACKS_TEXT) + 10
        openMusicPacksButton.x = width - openMusicPacksButton.width - 1
        openMusicPacksButton.y = 1

        packListWidget = PackBrowserListWidget(
            client!!,
            this.width,
            this.height - 96,
            48,
            this.height - 64,
            36
        ) { musicPack -> selectedPack = musicPack }

        doneButton = makeDoneButton(textRenderer, width, height) { client?.setScreen(parent) }

        refreshButton = ButtonWidget.builder(REFRESH_TEXT) { _: ButtonWidget? -> runBlocking { coroutineScope { reload() } } }.build()
        refreshButton.x = 1
        refreshButton.y = 1
        refreshButton.width = textRenderer.getWidth(REFRESH_TEXT) + 10

        lastRefreshedWidget = TextWidget(Text.empty(), textRenderer)
        lastRefreshedWidget.y = refreshButton.y + refreshButton.height + 3
        lastRefreshedWidget.alignLeft()
        lastRefreshedWidget.x = 2

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
        discordButton.y = openMusicPacksButton.y + openMusicPacksButton.height + 2
        discordButton.x = width - discordButton.width - 1

        addSelectableChild(packListWidget)
        addDrawableChild(openMusicPacksButton)
        addDrawableChild(doneButton)
        addDrawableChild(refreshButton)
        addDrawableChild(lastRefreshedWidget)
        addDrawableChild(discordButton)
    }

    override fun close() {
        client?.setScreen(parent)
    }

    override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        renderBackground(context)
        lastRefreshedWidget.message = this.packListWidget.refreshTime?.let {
            lastRefreshedWidget.active = true
            Text.literal("${LAST_REFRESHED_TEXT.string}: $it")
                .getWithStyle(Style.EMPTY.withColor(Colors.GRAY)).first()
        } ?: run {
            lastRefreshedWidget.active = false
            REFRESHING_TEXT
        }

        this.packListWidget.render(context, mouseX, mouseY, delta)
        super.render(context, mouseX, mouseY, delta)
        context?.drawCenteredTextWithShadow(
            this.textRenderer, this.title, this.width / 2, 28, Colors.WHITE)
    }

    fun reload() {
        packListWidget.reload(true)
    }

    companion object {
        private val OPEN_MUSIC_PACKS_TEXT = Text.translatableWithFallback(
            "trueadaptivemusic.open_pack_folder", "Open Pack Folder")
        private val REFRESH_TEXT = Text.translatableWithFallback("trueadaptivemusic.refresh", "Refresh")
        private val REFRESHING_TEXT = Text.translatableWithFallback(
            "trueadaptivemusic.refreshing", "Refreshing")
        val LAST_REFRESHED_TEXT: MutableText = Text.translatableWithFallback(
            "trueadaptivemusic.last_refreshed", "Last Refreshed")
    }
}