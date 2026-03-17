package liltojustice.trueadaptivemusic.client.gui.widget

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import liltojustice.trueadaptivemusic.Constants
import liltojustice.trueadaptivemusic.CurlHelper
import liltojustice.trueadaptivemusic.Logger
import liltojustice.trueadaptivemusic.client.TAMClient
import liltojustice.trueadaptivemusic.client.gui.RenderState
import liltojustice.trueadaptivemusic.client.gui.widget.utility.DownloadButtonWidget
import liltojustice.trueadaptivemusic.client.music.pack.BrowsableMusicPack
import liltojustice.trueadaptivemusic.client.music.pack.PackManifest
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.Click
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget
import net.minecraft.client.gui.widget.LoadingWidget
import net.minecraft.client.gui.widget.TextWidget
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Colors
import java.util.Date
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.io.path.Path
import kotlin.io.path.invariantSeparatorsPathString

class PackBrowserListWidget(
    client: MinecraftClient,
    width: Int,
    height: Int,
    top: Int,
    itemHeight: Int,
    private val onSelectPack: (selectedPack: BrowsableMusicPack) -> Unit = {}
) : AlwaysSelectedEntryListWidget<PackBrowserListWidget.Entry>(client, width, height, top, itemHeight) {
    private var packManifest: PackManifest? = null
    private var renderState = RenderState.Loading
    val refreshTime: Date?
        get() = packManifest?.timestamp

    private val backgroundScope = CoroutineScope(EmptyCoroutineContext)
    private val loadingWidget = LoadingWidget(this.client.textRenderer, LOADING_TEXT)
    private val noPacksFoundWidget = TextWidget(NO_PACKS_TEXT, client.textRenderer)
    private val loadFailureWidget = TextWidget(LOAD_FAILURE_TEXT, client.textRenderer)

    init {
        reload()
    }

    fun reload(ignoreCache: Boolean = false) {
        renderState = RenderState.Loading
        clearEntries()
        backgroundScope.launch {
            try {
                packManifest = TAMClient.fetchPacksFromRepository(ignoreCache)
                initEntries()

                renderState = RenderState.Success
            }
            catch (e: Exception) {
                Logger.logError("Failed to load packs: $e")
                renderState = RenderState.Failure
            }
        }
    }

    override fun renderWidget(context: DrawContext?, mouseX: Int, mouseY: Int, deltaTicks: Float) {
        super.renderWidget(context, mouseX, mouseY, deltaTicks)
        if (renderState == RenderState.Loading) {
            loadingWidget.setPosition(
                x + (width - loadingWidget.width) / 2, y + (height - loadingWidget.height) / 2)
            loadingWidget.render(context, mouseX, mouseY, deltaTicks)

            return
        }
        else if (renderState == RenderState.Failure) {
            loadFailureWidget.setPosition(
                x + (width - loadFailureWidget.width) / 2, y + (height - loadFailureWidget.height) / 2)
            loadFailureWidget.render(context, mouseX, mouseY, deltaTicks)

            return
        }

        if (packManifest == null) {
            noPacksFoundWidget.setPosition(
                x + (width - noPacksFoundWidget.width) / 2, y + (height - noPacksFoundWidget.height) / 2)
            noPacksFoundWidget.renderWidget(context, mouseX, mouseY, deltaTicks)

            return
        }
    }

    private fun initEntries() {
        packManifest?.packs?.forEach { addEntry(Entry(it)) }
    }

    companion object {
        val LOADING_TEXT: MutableText = Text.translatableWithFallback(
            "trueadaptivemusic.downloading_packs", "Downloading pack list")
        val NO_PACKS_TEXT: MutableText = Text.translatableWithFallback(
            "trueadaptivemusic.no_packs_found", "No packs found")
        val LOAD_FAILURE_TEXT: MutableText = Text.translatableWithFallback(
            "trueadaptivemusic.load_failed", "Failed to load packs")
    }

    inner class Entry(private val musicPack: BrowsableMusicPack): AlwaysSelectedEntryListWidget.Entry<Entry>() {
        private val downloadButton =
            DownloadButtonWidget {
                val curlUrl =
                    if (musicPack.source.startsWith(Constants.DISCORD_SOURCE_PREFIX))
                        musicPack.source
                    else if (musicPack.source.startsWith(Constants.DRIVE_SOURCE_PREFIX))
                        "${Constants.GOOGLE_DRIVE_LINK}/" +
                                musicPack.source.split("/").takeLast(2).first()
                    else
                        null

                runBlocking {
                    curlUrl?.let {
                        CurlHelper.curl(
                            curlUrl,
                            Path(
                                Constants.MUSIC_PACK_DIR.invariantSeparatorsPathString,
                                "${musicPack.name}-${musicPack.version}.zip"
                            )
                        )
                    } ?: throw Exception("Failed to get curl target for pack.")
                }
            }

        override fun render(
            context: DrawContext,
            mouseX: Int,
            mouseY: Int,
            hovered: Boolean,
            tickDelta: Float
        ) {
            context.drawText(
                client.textRenderer, musicPack.name, x + 3, y + 6, Colors.WHITE, false)
            context.drawText(
                client.textRenderer,
                musicPack.description,
                x + 3, y + 17,
                Colors.GRAY,
                false
            )

            downloadButton.x = x + width - downloadButton.width - 5
            downloadButton.y = y + height - downloadButton.height - 5
            downloadButton.render(context, mouseX, mouseY, tickDelta)
        }

        override fun mouseClicked(click: Click, doubled: Boolean): Boolean {
            if (!isMouseOver(click.x, click.y)) {
                return false
            }

            downloadButton.mouseClicked(click, doubled)
            setSelected(this)
            onSelectPack(musicPack)

            return true
        }

        override fun getNarration(): Text {
            return Text.empty()
        }
    }
}