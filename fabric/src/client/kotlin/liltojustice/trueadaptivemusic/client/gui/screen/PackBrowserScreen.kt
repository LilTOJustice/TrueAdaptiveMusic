package liltojustice.trueadaptivemusic.client.gui.screen

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import liltojustice.trueadaptivemusic.Constants
import liltojustice.trueadaptivemusic.client.browser.BrowsableMusicPack
import liltojustice.trueadaptivemusic.client.gui.widget.PackBrowserListWidget
import liltojustice.trueadaptivemusic.client.gui.widget.utility.makeDoneButton
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.Tooltip
import net.minecraft.client.gui.screens.ConfirmLinkScreen
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.util.CommonColors
import net.minecraft.util.Util
import java.util.Date

@Environment(EnvType.CLIENT)
class PackBrowserScreen(private val parent: Screen): Screen(
    Component.translatableWithFallback(
        "trueadaptivemusic.music_pack_browser", "Music Pack Browser")) {
    private lateinit var packListWidget: PackBrowserListWidget
    private lateinit var openMusicPacksButton: Button
    private lateinit var doneButton: Button
    private lateinit var refreshButton: Button
    private lateinit var discordButton: Button
    private var selectedPack: BrowsableMusicPack? = null
    private var refreshTime: Date? = null

    override fun init() {
        openMusicPacksButton = Button.Builder(OPEN_MUSIC_PACKS_TEXT) {
            Util.getPlatform().openUri(Constants.MUSIC_PACK_DIR.toUri())
        }.build()
        openMusicPacksButton.width = font.width(OPEN_MUSIC_PACKS_TEXT) + 10
        openMusicPacksButton.x = width - openMusicPacksButton.width - 1
        openMusicPacksButton.y = 1

        packListWidget = PackBrowserListWidget(
            minecraft, this.width, this.height - 96, 48, 36)
        { musicPack -> selectedPack = musicPack }

        doneButton = makeDoneButton(font, width, height) { minecraft.gui.setScreen(parent) }

        refreshButton = Button.builder(REFRESH_TEXT) { _: Button? -> runBlocking { coroutineScope { reload() } } }.build()
        refreshButton.x = 1
        refreshButton.y = 1
        refreshButton.width = font.width(REFRESH_TEXT) + 10

        discordButton = Button.builder(Constants.DISCORD_JOIN_TEXT)
        { _: Button? -> minecraft.gui.setScreen(
            ConfirmLinkScreen(
                { confirmed ->
                    if (confirmed) {
                        Util.getPlatform().openUri(Constants.DISCORD_JOIN_URL)
                    }

                    minecraft.gui.setScreen(this)
                },
                Constants.DISCORD_JOIN_URL,
                true
            )
        ) }.build()
        discordButton.width = font.width(Constants.DISCORD_JOIN_TEXT) + 10
        discordButton.y = openMusicPacksButton.y + openMusicPacksButton.height + 2
        discordButton.x = width - discordButton.width - 1

        addWidget(packListWidget)
        addRenderableWidget(openMusicPacksButton)
        addRenderableWidget(doneButton)
        addRenderableWidget(refreshButton)
        addRenderableWidget(discordButton)
    }

    override fun onClose() {
        minecraft.gui.setScreen(parent)
    }

    override fun extractRenderState(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, a: Float) {
        if (this.packListWidget.refreshTime != refreshTime) {
            refreshTime = this.packListWidget.refreshTime
            refreshTime.let {
                refreshButton.setTooltip(
                    Tooltip.create(
                        Component.literal("${LAST_REFRESHED_TEXT.string}: $it").withColor(CommonColors.GRAY))
                )
            }
        }

        super.extractRenderState(graphics, mouseX, mouseY, a)
        packListWidget.extractRenderState(graphics, mouseX, mouseY, a)
        graphics.centeredText(font, title, width / 2, 28, CommonColors.WHITE)
    }

    fun reload() {
        packListWidget.reload(true)
    }

    companion object {
        private val OPEN_MUSIC_PACKS_TEXT = Component.translatableWithFallback(
            "trueadaptivemusic.open_pack_folder", "Open Pack Folder")
        private val REFRESH_TEXT = Component.translatableWithFallback("trueadaptivemusic.refresh", "Refresh")
        private val LAST_REFRESHED_TEXT: MutableComponent = Component.translatableWithFallback(
            "trueadaptivemusic.last_refreshed", "Last Refreshed")
    }
}