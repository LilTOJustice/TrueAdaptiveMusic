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
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.ObjectSelectionList
import net.minecraft.client.gui.components.LoadingDotsWidget
import net.minecraft.client.gui.components.StringWidget
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Component
import net.minecraft.util.CommonColors
import net.minecraft.resources.ResourceLocation
import net.minecraft.Util
import net.minecraft.client.gui.screens.Screen.MENU_BACKGROUND
import net.minecraft.util.Mth
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
    client: Minecraft,
    width: Int,
    height: Int,
    top: Int,
    itemHeight: Int,
    private val onSelectPack: (selectedPack: BrowsableMusicPack) -> Unit = {}
) : ObjectSelectionList<PackBrowserListWidget.Entry>(client, width, height, top, itemHeight) {
    val refreshTime: Date?
        get() = packManifest?.timestamp

    private var packManifest: PackManifest? = null
    private var renderState = RenderState.Loading
    private val backgroundScope = CoroutineScope(EmptyCoroutineContext)
    private val loadingWidget = LoadingDotsWidget(this.minecraft.font, LOADING_TEXT)
    private val noPacksFoundWidget = StringWidget(NO_PACKS_TEXT, client.font)
    private val loadFailureWidget = StringWidget(LOAD_FAILURE_TEXT, client.font)
    private val downloadedPacks
        get() = Constants.MUSIC_PACK_DIR.toFile().listFiles().filter { it.extension == "zip" }.map { Path(it.path) }
    private val loadedPackImages = mutableSetOf<ResourceLocation>()
    private val failedPackImages = mutableSetOf<ResourceLocation>()

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

    override fun scrollBarX(): Int {
        return rowRight + 3
    }

    override fun getRowLeft(): Int {
        return x + 3
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
        val m = Mth.floor(y - this.y.toDouble()) - this.headerHeight + this.scrollAmount().toInt() - 4
        val n = m / this.itemHeight
        return this.children().takeIf {
            x >= j.toDouble() && x <= k.toDouble() && m >= 0 && n < this.itemCount
        }?.get(n)
    }

    override fun renderWidget(context: GuiGraphics, mouseX: Int, mouseY: Int, deltaTicks: Float) {
        context.blit(
            RenderType::guiTextured,
            MENU_BACKGROUND,
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

        selected?.let { renderSelectedPack(context, it.musicPack) }
    }

    private fun initEntries() {
        val entries = packManifest?.packs?.map { Entry(it) }
        entries?.forEach { addEntry(it) }
        setSelected(entries?.firstOrNull())
    }

    private fun renderSelectedPack(context: GuiGraphics?, musicPack: BrowsableMusicPack) {
        val panelX = scrollBarX() + 9
        val panelWidth = width - panelX
        context?.renderOutline(panelX, y, panelWidth - 1, height, CommonColors.WHITE)

        val restrictDescription = musicPack.getImagePath()?.let { imagePath ->
            if (!imagePath.exists()) {
                return@let false
            }

            renderPackImage(context, panelX, panelWidth, imagePath)
        } == true

        if (restrictDescription) {
            context?.vLine(panelX + panelWidth / 3, y, y + height, CommonColors.WHITE)
        }

        context?.drawString(
            minecraft.font,
            musicPack.name,
            panelX +
                    ((if (restrictDescription) panelWidth + panelWidth / 3 else panelWidth) -
                            minecraft.font.width(musicPack.name)) / 2,
            y + 3,
            CommonColors.WHITE
        )

        val flavorText = Component.empty()
            .append(Component.literal("Title:\n").withColor(CommonColors.GRAY))
            .append(musicPack.name)

        musicPack.author?.let {
            flavorText
                .append(Component.literal("\n\nAuthor:\n").withColor(CommonColors.GRAY))
                .append(it)
        }

        musicPack.version?.let {
            flavorText
                .append(Component.literal("\n\nVersion:\n").withColor(CommonColors.GRAY))
                .append(it)
        }

        musicPack.description?.let {
            flavorText
                .append(Component.literal("\n\nDescription:\n").withColor(CommonColors.GRAY))
                .append(it)
        }

        flavorText
            .append(Component.literal("\n\nSize:\n").withColor(CommonColors.GRAY))
            .append(Component.literal(DataSizeHelper.getDataSizeString(musicPack.size)))

        flavorText
            .append(Component.literal("\n\nUpdated:\n").withColor(CommonColors.GRAY))
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
            y + 3 + if (restrictDescription) 0 else (minecraft.font.lineHeight + 3),
            y + height - 3
        )
    }

    private fun renderPackImage(context: GuiGraphics?, panelX: Int, panelWidth: Int, imagePath: Path): Boolean {
        val identifier = ResourceLocation.fromNamespaceAndPath(
            "trueadaptivemusic",
            "image/" +
                    Util.sanitizeName(imagePath.name, ResourceLocation::validPathChar)
        )

        if (identifier in failedPackImages) {
            return false
        }

        if (identifier !in loadedPackImages) {
            ImageProcessor.getNativeImage(imagePath)?.let { image ->
                minecraft.textureManager.register(
                    identifier,
                    DynamicTexture(identifier::toString, image)
                )
            } ?: run {
                failedPackImages.add(identifier)

                return false
            }

            loadedPackImages.add(identifier)
        }

        val image = (minecraft.textureManager.getTexture(identifier) as? DynamicTexture)?.pixels
            ?: return false

        val imageY = y + minecraft.font.lineHeight + 6
        val aspectRatio = image.width.toFloat() / image.height
        val maxImageWidth = panelWidth * 2 / 3 - 6
        val maxImageHeight = y + height - imageY - 3
        var finalImageWidth = image.width
        var finalImageHeight = image.height
        val widthDiff = image.width - maxImageWidth
        val heightDiff = image.height - maxImageHeight
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

        context?.blit(
            RenderType::guiTextured,
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
        private val versionText = Component.literal("Ver ${musicPack.version}").withColor(CommonColors.GRAY)
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
            renderScrollingString(
                context,
                minecraft.font,
                Component.literal(musicPack.name),
                x + 3,
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

            renderScrollingString(
                context,
                minecraft.font,
                versionText,
                x + 3,
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
