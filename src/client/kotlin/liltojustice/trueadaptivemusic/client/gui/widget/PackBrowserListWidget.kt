package liltojustice.trueadaptivemusic.client.gui.widget

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import liltojustice.trueadaptivemusic.Constants
import liltojustice.trueadaptivemusic.Logger
import liltojustice.trueadaptivemusic.Reference
import liltojustice.trueadaptivemusic.client.TAMClient
import liltojustice.trueadaptivemusic.client.gui.RenderState
import liltojustice.trueadaptivemusic.client.gui.extensions.drawBorder
import liltojustice.trueadaptivemusic.client.gui.widget.utility.DownloadButtonWidget
import liltojustice.trueadaptivemusic.client.music.pack.browsable.BrowsableMusicPack
import liltojustice.trueadaptivemusic.client.music.pack.browsable.BrowsableMusicPackDownloader
import liltojustice.trueadaptivemusic.client.music.pack.browsable.PackManifest
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gl.RenderPipelines
import net.minecraft.client.gui.Click
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen.MENU_BACKGROUND_TEXTURE
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget
import net.minecraft.client.gui.widget.LoadingWidget
import net.minecraft.client.gui.widget.TextWidget
import net.minecraft.client.texture.NativeImage
import net.minecraft.client.texture.NativeImageBackedTexture
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Colors
import net.minecraft.util.Identifier
import net.minecraft.util.Util
import java.util.Date
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.io.path.exists
import kotlin.io.path.name

class PackBrowserListWidget(
    client: MinecraftClient,
    width: Int,
    height: Int,
    top: Int,
    itemHeight: Int,
    private val onSelectPack: (selectedPack: BrowsableMusicPack) -> Unit = {}
) : AlwaysSelectedEntryListWidget<PackBrowserListWidget.Entry>(client, width, height, top, itemHeight) {
    val refreshTime: Date?
        get() = packManifest?.timestamp

    private var packManifest: PackManifest? = null
    private var renderState = RenderState.Loading
    private val backgroundScope = CoroutineScope(EmptyCoroutineContext)
    private val loadingWidget = LoadingWidget(this.client.textRenderer, LOADING_TEXT)
    private val noPacksFoundWidget = TextWidget(NO_PACKS_TEXT, client.textRenderer)
    private val loadFailureWidget = TextWidget(LOAD_FAILURE_TEXT, client.textRenderer)
    private val downloadedPacks
        get() = Constants.MUSIC_PACK_DIR.toFile().listFiles().map { it.name }
    private val loadedPackImages = mutableSetOf<Identifier>()

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
                Logger.logError("Failed to load packs:\n$e")
                renderState = RenderState.Failure
            }
        }
    }

    override fun getRowLeft(): Int {
        return x + 3
    }

    override fun renderWidget(context: DrawContext?, mouseX: Int, mouseY: Int, deltaTicks: Float) {
        context?.drawTexture(
            RenderPipelines.GUI_TEXTURED,
            MENU_BACKGROUND_TEXTURE,
            x,
            y,
            0F,
            0F,
            width,
            height,
            32,
            32
        )

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

        selectedOrNull?.let {
            val panelX = scrollbarX + 9
            val panelWidth = width - panelX
            context?.drawBorder(panelX, y, panelWidth, height)
            context?.drawTextWithShadow(
                client.textRenderer,
                it.musicPack.name,
                panelX + (panelWidth - client.textRenderer.getWidth(it.musicPack.name)) / 2,
                y + 3,
                Colors.WHITE
            )
            it.musicPack.description?.let { description ->
                context?.drawWrappedText(
                    client.textRenderer,
                    Text.literal(description),
                    panelX + 3,
                    y + client.textRenderer.fontHeight + 6,
                    panelWidth / 3,
                    Colors.WHITE,
                    false
                )
            }

            it.musicPack.getImagePath()?.let { imagePath ->
                if (!imagePath.exists()) {
                    return@let
                }

                val identifier = Identifier.of(
                    "trueadaptivemusic",
                    Util.replaceInvalidChars(imagePath.name, Identifier::isPathCharacterValid)
                )

                if (identifier !in loadedPackImages) {
                    val nativeImage = NativeImage.read(imagePath.toFile().inputStream())
                    client.textureManager.registerTexture(
                        identifier,
                        NativeImageBackedTexture(identifier::toString, nativeImage)
                    )
                }

                val image = (client.textureManager.getTexture(identifier) as? NativeImageBackedTexture)?.image
                    ?: return@let

                val imageY = y + client.textRenderer.fontHeight + 6
                val aspectRatio = image.width.toFloat() / image.height
                val maxImageWidth = panelWidth * 2 / 3 - 6
                val maxImageHeight = y + height - imageY - 3
                var finalImageWidth = image.width
                var finalImageHeight = image.height
                val widthDiff = (image.width - maxImageWidth)
                val heightDiff = (image.height - maxImageHeight)

                if (widthDiff > heightDiff && widthDiff > 0) {
                    finalImageWidth = maxImageWidth
                    finalImageHeight = (finalImageWidth / aspectRatio).toInt()
                }
                else if (heightDiff > 0) {
                    finalImageHeight = maxImageHeight
                    finalImageWidth = (finalImageHeight * aspectRatio).toInt()
                }

                context?.drawTexture(
                    RenderPipelines.GUI_TEXTURED,
                    identifier,
                    panelX + 3 + panelWidth / 3,
                    imageY,
                    0F,
                    0F,
                    finalImageWidth,
                    finalImageHeight,
                    finalImageWidth,
                    finalImageHeight
                )
            }
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

    inner class Entry(val musicPack: BrowsableMusicPack): AlwaysSelectedEntryListWidget.Entry<Entry>() {
        private val progress = Reference(0F)
        private val versionText = Text.literal("Ver ${musicPack.version}").withColor(Colors.GRAY)
        private val downloadButton =
            DownloadButtonWidget(musicPack.getFilePath().name in downloadedPacks, progress) {
                runBlocking {
                    BrowsableMusicPackDownloader.downloadMusicPack(musicPack, progress)
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

            downloadButton.x = x + width - downloadButton.width - 5
            downloadButton.y = y + height - downloadButton.height - 5
            downloadButton.render(context, mouseX, mouseY, tickDelta)

            context.textConsumer.marqueedText(
                versionText,
                x + 3,
                x + 3,
                downloadButton.x - 3,
                y + 17,
                y + height
            )
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