package liltojustice.trueadaptivemusic.client.gui.widget

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import liltojustice.trueadaptivemusic.Constants
import liltojustice.trueadaptivemusic.Logger
import liltojustice.trueadaptivemusic.Reference
import liltojustice.trueadaptivemusic.client.TAMClient
import liltojustice.trueadaptivemusic.client.browser.BrowsableMusicPack
import liltojustice.trueadaptivemusic.client.browser.DataSizeHelper
import liltojustice.trueadaptivemusic.client.browser.DownloadButtonWidget
import liltojustice.trueadaptivemusic.client.browser.PackManifest
import liltojustice.trueadaptivemusic.client.gui.ImageProcessor
import liltojustice.trueadaptivemusic.client.gui.RenderState
import liltojustice.trueadaptivemusic.client.gui.extensions.drawMarqueedWrappedText
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen.MENU_BACKGROUND_TEXTURE
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget
import net.minecraft.client.gui.widget.LoadingWidget
import net.minecraft.client.gui.widget.TextWidget
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.texture.NativeImageBackedTexture
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Colors
import net.minecraft.util.Identifier
import net.minecraft.util.Util
import net.minecraft.util.math.MathHelper
import java.nio.file.Path
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.io.extension
import kotlin.io.path.Path
import kotlin.io.path.deleteIfExists
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
        get() = Constants.MUSIC_PACK_DIR.toFile().listFiles().filter { it.extension == "zip" }.map { Path(it.path) }
    private val loadedPackImages = mutableSetOf<Identifier>()

    init {
        reload(firstLoad)

        if (firstLoad) {
            firstLoad = false
        }
    }

    fun reload(ignoreCache: Boolean = false) {
        loadedPackImages.clear()
        renderState = RenderState.Loading
        clearEntries()
        backgroundScope.launch {
            try {
                packManifest = TAMClient.extensions?.packFetcher(ignoreCache)
                initEntries()
                renderState = RenderState.Success
            }
            catch (e: Exception) {
                Logger.logError("Failed to load packs:\n$e")
                renderState = RenderState.Failure
            }
        }
    }

    override fun getScrollbarX(): Int {
        return rowRight + 3
    }

    override fun getRowLeft(): Int {
        return x + 3
    }

    override fun drawSelectionHighlight(
        context: DrawContext,
        y: Int,
        entryWidth: Int,
        entryHeight: Int,
        borderColor: Int,
        fillColor: Int
    ) {
        val i = rowLeft
        val j = rowRight
        context.fill(i, y - 2, j, y + entryHeight + 2, borderColor)
        context.fill(i + 1, y - 1, j - 1, y + entryHeight + 1, fillColor)
    }

    override fun getEntryAtPosition(x: Double, y: Double): Entry? {
        val j = rowLeft
        val k = rowRight
        val m = MathHelper.floor(y - this.y.toDouble()) - this.headerHeight + this.scrollY.toInt() - 4
        val n = m / this.itemHeight
        return this.children().takeIf {
            x >= j.toDouble() && x <= k.toDouble() && m >= 0 && n < this.entryCount
        }?.get(n)
    }

    override fun renderWidget(context: DrawContext?, mouseX: Int, mouseY: Int, deltaTicks: Float) {
        context?.drawTexture(
            RenderLayer::getGuiTextured,
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

        selectedOrNull?.let { renderSelectedPack(context, it.musicPack) }
    }

    private fun initEntries() {
        val entries = packManifest?.packs?.map { Entry(it) }
        entries?.forEach { addEntry(it) }
        setSelected(entries?.firstOrNull())
    }

    private fun renderSelectedPack(context: DrawContext?, musicPack: BrowsableMusicPack) {
        val panelX = scrollbarX + 9
        val panelWidth = width - panelX
        context?.drawBorder(panelX, y, panelWidth - 1, height, Colors.WHITE)

        val restrictDescription = musicPack.getImagePath()?.let { imagePath ->
            if (!imagePath.exists()) {
                return@let false
            }

            renderPackImage(context, panelX, panelWidth, imagePath)
        } == true

        if (restrictDescription) {
            context?.drawVerticalLine(panelX + panelWidth / 3, y, y + height, Colors.WHITE)
        }

        context?.drawTextWithShadow(
            client.textRenderer,
            musicPack.name,
            panelX +
                    ((if (restrictDescription) panelWidth + panelWidth / 3 else panelWidth) -
                            client.textRenderer.getWidth(musicPack.name)) / 2,
            y + 3,
            Colors.WHITE
        )

        val flavorText = Text.empty()
            .append(Text.literal("Title:\n").withColor(Colors.GRAY))
            .append(musicPack.name)

        musicPack.author?.let {
            flavorText
                .append(Text.literal("\n\nAuthor:\n").withColor(Colors.GRAY))
                .append(it)
        }

        musicPack.version?.let {
            flavorText
                .append(Text.literal("\n\nVersion:\n").withColor(Colors.GRAY))
                .append(it)
        }

        musicPack.description?.let {
            flavorText
                .append(Text.literal("\n\nDescription:\n").withColor(Colors.GRAY))
                .append(it)
        }

        flavorText
            .append(Text.literal("\n\nSize:\n").withColor(Colors.GRAY))
            .append(Text.literal(DataSizeHelper.getDataSizeString(musicPack.size)))

        flavorText
            .append(Text.literal("\n\nUpdated:\n").withColor(Colors.GRAY))
            .append(
                Text.literal(SimpleDateFormat("EEE MMM dd yyyy").format(musicPack.lastUpdated)))
            .append(
                Text.literal("\n" + SimpleDateFormat("hh:mm aa zzz").format(musicPack.lastUpdated)))

        val flavorTextWidth = (if (restrictDescription) panelWidth / 3 else panelWidth) - 3
        val flavorTextX = panelX + 3
        context?.drawMarqueedWrappedText(
            client.textRenderer,
            flavorText,
            flavorTextX,
            flavorTextX + flavorTextWidth,
            y + 3 + if (restrictDescription) 0 else (client.textRenderer.fontHeight + 3),
            y + height - 3
        )
    }

    private fun renderPackImage(context: DrawContext?, panelX: Int, panelWidth: Int, imagePath: Path): Boolean {
        val identifier = Identifier.of(
            "trueadaptivemusic",
            "image/" +
                    Util.replaceInvalidChars(imagePath.name, Identifier::isPathCharacterValid)
        )

        if (identifier !in loadedPackImages) {
            ImageProcessor.getNativeImage(imagePath)?.let { image ->
                client.textureManager.registerTexture(
                    identifier,
                    NativeImageBackedTexture(image)
                )
            } ?: return false
            loadedPackImages.add(identifier)
        }

        val image = (client.textureManager.getTexture(identifier) as? NativeImageBackedTexture)?.image
            ?: return false

        val imageY = y + client.textRenderer.fontHeight + 6
        val aspectRatio = image.width.toFloat() / image.height
        val maxImageWidth = panelWidth * 2 / 3 - 6
        val maxImageHeight = y + height - imageY - 3
        var finalImageWidth = image.width
        var finalImageHeight = image.height
        val widthDiff = (image.width - maxImageWidth)
        val heightDiff = (image.height - maxImageHeight)
        var xOffset = 0
        var yOffset = 0

        if (widthDiff > heightDiff && widthDiff > 0) {
            finalImageWidth = maxImageWidth
            finalImageHeight = (finalImageWidth / aspectRatio).toInt()
            yOffset = (height - finalImageHeight) / 2
        }
        else if (heightDiff > 0) {
            finalImageHeight = maxImageHeight
            finalImageWidth = (finalImageHeight * aspectRatio).toInt()
            xOffset = (panelWidth * 2 / 3 - finalImageWidth) / 2
        }

        context?.drawTexture(
            RenderLayer::getGuiTextured,
            identifier,
            panelX + 3 + panelWidth / 3 + xOffset,
            imageY + yOffset,
            0F,
            0F,
            finalImageWidth,
            finalImageHeight,
            finalImageWidth,
            finalImageHeight
        )

        return true
    }

    companion object {
        private var firstLoad = true
        private val LOADING_TEXT: MutableText = Text.translatableWithFallback(
            "trueadaptivemusic.downloading_packs", "Downloading Pack List")
        private val NO_PACKS_TEXT: MutableText = Text.translatableWithFallback(
            "trueadaptivemusic.no_packs_found", "No Packs Found")
        private val LOAD_FAILURE_TEXT: MutableText = Text.translatableWithFallback(
            "trueadaptivemusic.load_failed", "Failed to Load Packs")
    }

    inner class Entry(val musicPack: BrowsableMusicPack): AlwaysSelectedEntryListWidget.Entry<Entry>() {
        private val progress = Reference(0F)
        private val versionText = Text.literal("Ver ${musicPack.version}").withColor(Colors.GRAY)
        private val packPath = musicPack.getFilePath()
        private val downloadButton = run {
            val isDownloaded = packPath in downloadedPacks
            val oldPack = downloadedPacks
                .takeIf { !isDownloaded }
                ?.firstOrNull { it.name.contains(musicPack.name) }
                ?.takeIf { it.toFile().lastModified() < musicPack.lastUpdated.time }

            DownloadButtonWidget(packPath in downloadedPacks, oldPack != null, progress) {
                runBlocking {
                    TAMClient.extensions?.packDownloader(musicPack, progress) ?: return@runBlocking
                    oldPack?.let { oldPack.deleteIfExists() }
                }
            }
        }

        override fun render(
            context: DrawContext,
            index: Int,
            y: Int,
            x: Int,
            entryWidth: Int,
            entryHeight: Int,
            mouseX: Int,
            mouseY: Int,
            hovered: Boolean,
            tickDelta: Float
        ) {
            drawScrollableText(
                context,
                client.textRenderer,
                Text.literal(musicPack.name),
                x + 3,
                x + 3,
                y,
                rowRight - 3,
                y + client.textRenderer.fontHeight + 3,
                Colors.WHITE
            )
            downloadButton.x = x + entryWidth - downloadButton.width - 5
            downloadButton.y = y + entryHeight - downloadButton.height - 2
            downloadButton.render(context, mouseX, mouseY, tickDelta)

            if (downloadButton.downloadStatus == RenderState.Loading) {
                val currentBytes = (progress.value * musicPack.size).toLong()
                val currentString = DataSizeHelper.getDataSizeString(currentBytes)
                val totalString = DataSizeHelper.getDataSizeString(musicPack.size)
                val percentString = String.format("%.1f", currentBytes.toFloat() / musicPack.size * 100) + '%'
                val progressText = Text.literal("$currentString/$totalString ($percentString)")
                context.drawText(
                    client.textRenderer,
                    progressText,
                    x + entryWidth - client.textRenderer.getWidth(progressText) - 5,
                    downloadButton.y - client.textRenderer.fontHeight,
                    Colors.GRAY,
                    false
                )
            }
            else {
                val sizeText = Text.literal(DataSizeHelper.getDataSizeString(musicPack.size))
                context.drawText(
                    client.textRenderer,
                    sizeText,
                    x + entryWidth - client.textRenderer.getWidth(sizeText) - 5,
                    downloadButton.y - client.textRenderer.fontHeight,
                    Colors.GRAY,
                    false
                )
            }

            drawScrollableText(
                context,
                client.textRenderer,
                versionText,
                x + 3,
                x + 3,
                y + 17,
                downloadButton.x - 3,
                y + entryHeight,
                Colors.GRAY
            )
        }

        override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
            if (!isMouseOver(mouseX, mouseY)) {
                return false
            }

            downloadButton.mouseClicked(mouseX, mouseY, button)
            setSelected(this)
            onSelectPack(musicPack)

            return true
        }

        override fun getNarration(): Text {
            return Text.empty()
        }
    }
}
