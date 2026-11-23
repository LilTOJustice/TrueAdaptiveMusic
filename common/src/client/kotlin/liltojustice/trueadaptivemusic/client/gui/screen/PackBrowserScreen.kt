package liltojustice.trueadaptivemusic.client.gui.screen

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import liltojustice.trueadaptivemusic.Constants
import liltojustice.trueadaptivemusic.Constants.TRUE_ADAPTIVE_MUSIC
import liltojustice.trueadaptivemusic.client.TAMClient
import liltojustice.trueadaptivemusic.client.browser.BrowsableMusicPack
import liltojustice.trueadaptivemusic.client.extensions.SuggestedPack
import liltojustice.trueadaptivemusic.client.gui.widget.PackBrowserListWidget
import liltojustice.trueadaptivemusic.client.gui.widget.utility.makeDoneButton
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.ConfirmLinkScreen
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.components.Tooltip
import net.minecraft.client.gui.components.Button
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Component
import net.minecraft.util.CommonColors
import net.minecraft.util.Util
import java.util.Date
import kotlin.io.path.invariantSeparatorsPathString

class PackBrowserScreen(private val parent: Screen): Screen(
    Component.translatableWithFallback("trueadaptivemusic.music_pack_browser", "Music Pack Browser")) {
    private lateinit var packListWidget: PackBrowserListWidget
    private lateinit var openMusicPacksButton: Button
    private lateinit var doneButton: Button
    private lateinit var refreshButton: Button
    private lateinit var discordButton: Button
    private lateinit var setSuggestedPackButton: Button
    private var selectedPack: BrowsableMusicPack? = null
    private var refreshTime: Date? = null
    private var suggestedPack: SuggestedPack? = TAMClient.getSuggestedPack()

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

        doneButton = makeDoneButton(font, width, height) { minecraft.setScreen(parent) }

        refreshButton = Button.builder(REFRESH_TEXT) { _: Button? ->
            runBlocking { coroutineScope { reload() } }
        }
            .build()
        refreshButton.x = 1
        refreshButton.y = 1
        refreshButton.width = font.width(REFRESH_TEXT) + 10

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
        discordButton.y = openMusicPacksButton.y + openMusicPacksButton.height + 2
        discordButton.x = width - discordButton.width - 1

        setSuggestedPackButton = Button.builder(SET_SUGGESTED_PACK_TEXT)
        { _: Button? ->
            selectedPack?.let {
                SuggestedPack(it.name, it.version ?: "").let { newSuggestedPack ->
                    suggestedPack = newSuggestedPack
                    TAMClient.setSuggestedPack(newSuggestedPack)
                }
            }
        }.build()
        setSuggestedPackButton.width = font.width(SET_SUGGESTED_PACK_TEXT) + 10
        setSuggestedPackButton.y = refreshButton.y + refreshButton.height + 2
        setSuggestedPackButton.x = refreshButton.x
        setSuggestedPackButton.setTooltip(Tooltip.create(SET_SUGGESTED_PACK_TOOLTIP_TEXT))

        addWidget(packListWidget)
        addRenderableWidget(openMusicPacksButton)
        addRenderableWidget(doneButton)
        addRenderableWidget(refreshButton)
        addRenderableWidget(discordButton)
        addRenderableWidget(setSuggestedPackButton)
    }

    override fun onClose() {
        minecraft.setScreen(parent)
    }

    override fun render(graphics: GuiGraphics, mouseX: Int, mouseY: Int, a: Float) {
        setSuggestedPackButton.visible = selectedPack != null
        setSuggestedPackButton.active = suggestedPack != selectedPack
        if (this.packListWidget.refreshTime != refreshTime) {
            refreshTime = this.packListWidget.refreshTime
            refreshTime.let {
                refreshButton.setTooltip(
                    Tooltip.create(
                        Component.literal("${LAST_REFRESHED_TEXT.string}: $it").withColor(CommonColors.GRAY))
                )
            }
        }

        super.render(graphics, mouseX, mouseY, a)
        this.packListWidget.render(graphics, mouseX, mouseY, a)
        graphics.drawCenteredString(
            this.font, this.title, this.width / 2, 28, CommonColors.WHITE)
    }

    fun reload() {
        packListWidget.reload(true)
    }

    companion object {
        private val OPEN_MUSIC_PACKS_TEXT = Component.translatableWithFallback(
            "trueadaptivemusic.open_pack_folder", "Open Pack Folder")
        private val REFRESH_TEXT = Component.translatableWithFallback("trueadaptivemusic.refresh", "Refresh")
        val LAST_REFRESHED_TEXT: MutableComponent = Component.translatableWithFallback(
            "trueadaptivemusic.last_refreshed", "Last Refreshed")
        private val SET_SUGGESTED_PACK_TEXT: MutableComponent = Component.translatableWithFallback(
            "$TRUE_ADAPTIVE_MUSIC.set_suggested_pack", "Set Suggested Pack")
        private val SET_SUGGESTED_PACK_TOOLTIP_TEXT: MutableComponent = Component.translatableWithFallback(
            "$TRUE_ADAPTIVE_MUSIC.set_suggested_pack_tooltip", "For modpack creators: Set the " +
                    "selected pack as the 'suggested' pack, which will auto-download and select the pack on first " +
                    "launch for your users! This means you won't need to include the music pack in your uploaded " +
                    "modpack. Be sure to include the ${Constants.SUGGESTED_PACK_PATH.invariantSeparatorsPathString} " +
                    "config!")
    }
}