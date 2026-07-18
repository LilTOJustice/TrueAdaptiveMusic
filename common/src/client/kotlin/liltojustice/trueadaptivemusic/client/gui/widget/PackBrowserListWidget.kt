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
import liltojustice.trueadaptivemusic.client.gui.widget.utility.drawScrollableText
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.ObjectSelectionList
import net.minecraft.client.gui.components.StringWidget
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.Component
import net.minecraft.util.CommonColors
import net.minecraft.resources.ResourceLocation
import net.minecraft.Util
import net.minecraft.util.Mth
import java.nio.file.Path
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.io.path.Path
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.io.path.name

class PackBrowserListWidget(
    client: Minecraft,
    width: Int,
    height: Int,
    top: Int,
    bottom: Int,
    itemHeight: Int,
    private val onSelectPack: (selectedPack: BrowsableMusicPack) -> Unit = {}
): ObjectSelectionList<PackBrowserListWidget.Entry>(
    client, width, height, top, bottom, itemHeight) {
    val refreshTime: Date?
        get() = packManifest?.timestamp

    private var packManifest: PackManifest? = null
    private var renderState = RenderState.Loading
    private val backgroundScope = CoroutineScope(EmptyCoroutineContext)
    private val noPacksFoundWidget = StringWidget(NO_PACKS_TEXT, client.font)
    private val loadFailureWidget = StringWidget(LOAD_FAILURE_TEXT, client.font)
    private val downloadedPacks
        get() = Constants.MUSIC_PACK_DIR
            .toFile().listFiles().filter { it.extension == "zip" }.map { Path(it.path) }
    private val loadedPackImages = mutableSetOf<ResourceLocation>()

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

    override fun getScrollbarPosition(): Int {
        return rowRight + 3
    }

    override fun getRowLeft(): Int {
        return 3
    }

    override fun renderSelection(
        context: GuiGraphics,
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
        val m = Mth.floor(y - y0.toDouble()) - this.headerHeight + this.scrollAmount.toInt() - 4
        val n = m / this.itemHeight
        return this.children().takeIf {
            x >= j.toDouble() && x <= k.toDouble() && m >= 0 && n < this.itemCount
        }?.get(n)
    }

    override fun render(context: GuiGraphics, mouseX: Int, mouseY: Int, deltaTicks: Float) {
        super.render(context, mouseX, mouseY, deltaTicks)
        if (renderState == RenderState.Loading) {
            context.drawString(
                minecraft.font,
                LOADING_TEXT,
                rowLeft + (width - minecraft.font.width(LOADING_TEXT)) / 2,
                y0 + (height - minecraft.font.lineHeight) / 2,
                CommonColors.WHITE,
                false
            )

            return
        }
        else if (renderState == RenderState.Failure) {
            loadFailureWidget.setPosition(
                rowLeft + (width - loadFailureWidget.width) / 2, y0 + (height - loadFailureWidget.height) / 2)
            loadFailureWidget.render(context, mouseX, mouseY, deltaTicks)

            return
        }

        if (packManifest == null) {
            noPacksFoundWidget.setPosition(
                rowLeft + (width - noPacksFoundWidget.width) / 2, y0 + (height - noPacksFoundWidget.height) / 2)
            noPacksFoundWidget.render(context, mouseX, mouseY, deltaTicks)

            return
        }

        selected?.let { renderSelectedPack(context, it.musicPack) }
    }

    private fun initEntries() {
        val entries = packManifest?.packs?.map { Entry(it) }
        entries?.forEach { addEntry(it) }
        setSelected(entries?.firstOrNull())
    }

    private fun renderSelectedPack(context: GuiGraphics?, musicPack: BrowsableMusicPack) {
        val panelX = scrollbarPosition + 9
        val panelWidth = width - panelX
        context?.renderOutline(panelX, y0, panelWidth - 1, y1 - y0, CommonColors.WHITE)

        val restrictDescription = musicPack.getImagePath()?.let { imagePath ->
            if (!imagePath.exists()) {
                return@let false
            }

            renderPackImage(context, panelX, panelWidth, imagePath)
        } == true

        if (restrictDescription) {
            context?.vLine(panelX + panelWidth / 3, y0, y1, CommonColors.WHITE)
        }

        context?.drawString(
            minecraft.font,
            musicPack.name,
            panelX +
                    ((if (restrictDescription) panelWidth + panelWidth / 3 else panelWidth) -
                            minecraft.font.width(musicPack.name)) / 2,
            y0 + 3,
            CommonColors.WHITE
        )

        val flavorText = Component.empty()
            .append(Component.literal("Title:\n").toFlatList(Style.EMPTY.withColor(CommonColors.GRAY)).first())
            .append(musicPack.name)

        musicPack.author?.let {
            flavorText
                .append(Component.literal("\n\nAuthor:\n").setStyle(Style.EMPTY.withColor(CommonColors.GRAY)))
                .append(it)
        }

        musicPack.version?.let {
            flavorText
                .append(
                    Component.literal("\n\nVersion:\n")
                        .toFlatList(Style.EMPTY.withColor(CommonColors.GRAY)).first()
                )
                .append(it)
        }

        musicPack.description?.let {
            flavorText
                .append(
                    Component.literal("\n\nDescription:\n")
                        .toFlatList(Style.EMPTY.withColor(CommonColors.GRAY)).first()
                )
                .append(it)
        }

        flavorText
            .append(Component.literal("\n\nSize:\n").toFlatList(Style.EMPTY.withColor(CommonColors.GRAY)).first())
            .append(Component.literal(DataSizeHelper.getDataSizeString(musicPack.size)))

        flavorText
            .append(
                Component.literal("\n\nUpdated:\n").toFlatList(Style.EMPTY.withColor(CommonColors.GRAY)).first())
            .append(
                Component.literal(SimpleDateFormat("EEE MMM dd yyyy").format(musicPack.lastUpdated)))
            .append(
                Component.literal("\n" + SimpleDateFormat("hh:mm aa zzz").format(musicPack.lastUpdated)))

        val flavorTextWidth = (if (restrictDescription) panelWidth / 3 else panelWidth) - 3
        val flavorTextX = panelX + 3
        context?.drawMarqueedWrappedText(
            minecraft.font,
            flavorText,
            flavorTextX,
            flavorTextX + flavorTextWidth,
            y0 + 3 + if (restrictDescription) 0 else (minecraft.font.lineHeight + 3),
            y1 - 3
        )
    }

    private fun renderPackImage(context: GuiGraphics?, panelX: Int, panelWidth: Int, imagePath: Path): Boolean {
        val identifier = ResourceLocation.tryBuild(
            "trueadaptivemusic",
            "image/" +
                    Util.sanitizeName(imagePath.name, ResourceLocation::validPathChar)
        ) ?: return false

        if (identifier !in loadedPackImages) {
            ImageProcessor.getNativeImage(imagePath)?.let { image ->
                minecraft.textureManager.register(
                    identifier,
                    DynamicTexture(image)
                )
            } ?: return false
            loadedPackImages.add(identifier)
        }

        val image = (minecraft.textureManager.getTexture(identifier) as? DynamicTexture)?.pixels
            ?: return false

        val imageY = y0 + minecraft.font.lineHeight + 6
        val aspectRatio = image.width.toFloat() / image.height
        val maxImageWidth = panelWidth * 2 / 3 - 6
        val maxImageHeight = y1 - imageY - 3
        var finalImageWidth = image.width
        var finalImageHeight = image.height
        val widthDiff = (image.width - maxImageWidth)
        val heightDiff = (image.height - maxImageHeight)
        var xOffset = 0
        var yOffset = 0

        if (widthDiff > heightDiff && widthDiff > 0) {
            finalImageWidth = maxImageWidth
            finalImageHeight = (finalImageWidth / aspectRatio).toInt()
            yOffset = (maxImageHeight - finalImageHeight) / 2
        }
        else if (heightDiff > 0) {
            finalImageHeight = maxImageHeight
            finalImageWidth = (finalImageHeight * aspectRatio).toInt()
            xOffset = (panelWidth * 2 / 3 - finalImageWidth) / 2
        }

        context?.blit(
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
        private val LOADING_TEXT: MutableComponent = Component.translatableWithFallback(
            "trueadaptivemusic.downloading_packs", "Downloading Pack List")
        private val NO_PACKS_TEXT: MutableComponent = Component.translatableWithFallback(
            "trueadaptivemusic.no_packs_found", "No Packs Found")
        private val LOAD_FAILURE_TEXT: MutableComponent = Component.translatableWithFallback(
            "trueadaptivemusic.load_failed", "Failed to Load Packs")
    }

    inner class Entry(val musicPack: BrowsableMusicPack): ObjectSelectionList.Entry<Entry>() {
        private val progress = Reference(0F)
        private val versionText = Component.literal(
            "Ver ${musicPack.version}").toFlatList(Style.EMPTY.withColor(CommonColors.GRAY)).first()
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
            context: GuiGraphics,
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
                minecraft.font,
                Component.literal(musicPack.name),
                x + 3,
                y,
                rowRight - 3,
                y + minecraft.font.lineHeight + 3,
                CommonColors.WHITE
            )
            downloadButton.x = x + entryWidth - downloadButton.width - 5
            downloadButton.y = y + entryHeight - downloadButton.height - 2
            downloadButton.render(context, mouseX, mouseY, tickDelta)

            if (downloadButton.downloadStatus == RenderState.Loading) {
                val currentBytes = (progress.value * musicPack.size).toLong()
                val currentString = DataSizeHelper.getDataSizeString(currentBytes)
                val totalString = DataSizeHelper.getDataSizeString(musicPack.size)
                val percentString = String.format("%.1f", currentBytes.toFloat() / musicPack.size * 100) + '%'
                val progressText = Component.literal("$currentString/$totalString ($percentString)")
                context.drawString(
                    minecraft.font,
                    progressText,
                    x + entryWidth - minecraft.font.width(progressText) - 5,
                    downloadButton.y - minecraft.font.lineHeight,
                    CommonColors.GRAY,
                    false
                )
            }
            else {
                val sizeText = Component.literal(DataSizeHelper.getDataSizeString(musicPack.size))
                context.drawString(
                    minecraft.font,
                    sizeText,
                    x + entryWidth - minecraft.font.width(sizeText) - 5,
                    downloadButton.y - minecraft.font.lineHeight,
                    CommonColors.GRAY,
                    false
                )
            }

            drawScrollableText(
                context,
                minecraft.font,
                versionText,
                x + 3,
                y + 17,
                downloadButton.x - 3,
                y + entryHeight,
                CommonColors.GRAY
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

        override fun getNarration(): Component {
            return Component.empty()
        }
    }
}