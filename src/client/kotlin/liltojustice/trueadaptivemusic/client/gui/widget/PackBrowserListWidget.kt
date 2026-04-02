package liltojustice.trueadaptivemusic.client.gui.widget

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import liltojustice.trueadaptivemusic.Constants
import liltojustice.trueadaptivemusic.DataSizeHelper
import liltojustice.trueadaptivemusic.Logger
import liltojustice.trueadaptivemusic.Reference
import liltojustice.trueadaptivemusic.client.TAMClient
import liltojustice.trueadaptivemusic.client.gui.ImageProcessor
import liltojustice.trueadaptivemusic.client.gui.RenderState
import liltojustice.trueadaptivemusic.client.gui.extensions.drawBorder
import liltojustice.trueadaptivemusic.client.gui.text.drawMarqueedWrappedText
import liltojustice.trueadaptivemusic.client.gui.widget.utility.DownloadButtonWidget
import liltojustice.trueadaptivemusic.client.music.pack.browsable.BrowsableMusicPack
import liltojustice.trueadaptivemusic.client.music.pack.browsable.BrowsableMusicPackDownloader
import liltojustice.trueadaptivemusic.client.music.pack.browsable.PackManifest
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.LoadingDotsWidget
import net.minecraft.client.gui.components.MultiLineTextWidget
import net.minecraft.client.gui.components.ObjectSelectionList
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.resources.Identifier
import net.minecraft.util.CommonColors
import net.minecraft.util.Util
import java.nio.file.Path
import java.text.SimpleDateFormat
import java.util.Date
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
    itemHeight: Int,
    private val onSelectPack: (selectedPack: BrowsableMusicPack) -> Unit = {}
): ObjectSelectionList<PackBrowserListWidget.Entry>(client, width, height, top, itemHeight) {
    val refreshTime: Date?
        get() = packManifest?.timestamp

    private var packManifest: PackManifest? = null
    private var renderState = RenderState.Loading
    private val backgroundScope = CoroutineScope(EmptyCoroutineContext)
    private val loadingWidget = LoadingDotsWidget(this.minecraft.font, LOADING_TEXT)
    private val noPacksFoundWidget = MultiLineTextWidget(NO_PACKS_TEXT, client.font)
    private val loadFailureWidget = MultiLineTextWidget(LOAD_FAILURE_TEXT, client.font)
    private val downloadedPacks
        get() = Constants.MUSIC_PACK_DIR
            .toFile().listFiles().filter { it.extension == "zip" }.map { Path(it.path) }
    private val loadedPackImages = mutableSetOf<Identifier>()

    init {
        reload()
    }

    fun reload(ignoreCache: Boolean = false) {
        loadedPackImages.clear()
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

    override fun extractWidgetRenderState(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, a: Float) {
        graphics.blit(
            RenderPipelines.GUI_TEXTURED,
            Screen.MENU_BACKGROUND,
            x,
            y,
            0F,
            0F,
            width,
            height,
            32,
            32
        )

        super.extractWidgetRenderState(graphics, mouseX, mouseY, a)
        if (renderState == RenderState.Loading) {
            loadingWidget.setPosition(
                x + (width - loadingWidget.width) / 2, y + (height - loadingWidget.height) / 2)
            loadingWidget.extractRenderState(graphics, mouseX, mouseY, a)

            return
        }
        else if (renderState == RenderState.Failure) {
            loadFailureWidget.setPosition(
                x + (width - loadFailureWidget.width) / 2, y + (height - loadFailureWidget.height) / 2)
            loadFailureWidget.extractRenderState(graphics, mouseX, mouseY, a)

            return
        }

        if (packManifest == null) {
            noPacksFoundWidget.setPosition(
                x + (width - noPacksFoundWidget.width) / 2, y + (height - noPacksFoundWidget.height) / 2)
            noPacksFoundWidget.extractRenderState(graphics, mouseX, mouseY, a)

            return
        }

        selected?.let { renderSelectedPack(graphics, it.musicPack) }
    }

    private fun initEntries() {
        val entries = packManifest?.packs?.map { Entry(it) }
        entries?.forEach { addEntry(it) }
        selected = entries?.firstOrNull()
    }

    private fun renderSelectedPack(graphics: GuiGraphicsExtractor, musicPack: BrowsableMusicPack) {
        val panelX = scrollBarX() + 9
        val panelWidth = width - panelX
        graphics.drawBorder(panelX, y, panelWidth - 1, height)

        val restrictDescription = musicPack.getImagePath()?.let { imagePath ->
            if (!imagePath.exists()) {
                return@let false
            }

            renderPackImage(graphics, panelX, panelWidth, imagePath)
        } == true

        if (restrictDescription) {
            graphics.verticalLine(panelX + panelWidth / 3, y, y + height, CommonColors.WHITE)
        }

        graphics.text(
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
                Component.literal(
                    SimpleDateFormat("EEE MMM dd yyyy").format(musicPack.lastUpdated))
            )
            .append(
                Component.literal(
                    "\n" + SimpleDateFormat("hh:mm aa zzz").format(musicPack.lastUpdated))
            )

        val flavorTextWidth = (if (restrictDescription) panelWidth / 3 else panelWidth) - 3
        val flavorTextX = panelX + 3
        graphics.drawMarqueedWrappedText(
            minecraft.font,
            flavorText,
            flavorTextX,
            flavorTextX + flavorTextWidth,
            y + 3 + if (restrictDescription) 0 else (minecraft.font.lineHeight + 3),
            y + height - 3
        )
    }

    private fun renderPackImage(
        graphics: GuiGraphicsExtractor, panelX: Int, panelWidth: Int, imagePath: Path): Boolean {
        val identifier = Identifier.fromNamespaceAndPath(
            "trueadaptivemusic",
            "image/${Util.sanitizeName(imagePath.name, Identifier::validPathChar)}"
        )

        if (identifier !in loadedPackImages) {
            minecraft.textureManager.register(
                identifier,
                DynamicTexture(identifier::toString, ImageProcessor.getNativeImage(imagePath))
            )
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

        graphics.blit(
            RenderPipelines.GUI_TEXTURED,
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

            DownloadButtonWidget(
                packPath in downloadedPacks, oldPack != null, progress) {
                runBlocking {
                    BrowsableMusicPackDownloader.downloadMusicPack(musicPack, progress)
                    oldPack?.let { oldPack.deleteIfExists() }
                }
            }
        }

        override fun extractContent(
            graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, hovered: Boolean, a: Float) {
            graphics.textRenderer().acceptScrolling(
                Component.literal(musicPack.name),
                x + 3,
                x + 3,
                rowRight - 3,
                y + 3,
                y + minecraft.font.lineHeight + 3,
            )
            downloadButton.x = x + width - downloadButton.width - 5
            downloadButton.y = y + height - downloadButton.height - 5
            downloadButton.extractRenderState(graphics, mouseX, mouseY, a)

            if (downloadButton.downloadStatus == RenderState.Loading) {
                val currentBytes = (progress.value * musicPack.size).toLong()
                val currentString = DataSizeHelper.getDataSizeString(currentBytes)
                val totalString = DataSizeHelper.getDataSizeString(musicPack.size)
                val percentString = String.format("%.1f", currentBytes.toFloat() / musicPack.size * 100) + '%'
                val progressText = Component.literal("$currentString/$totalString ($percentString)")
                graphics.text(
                    minecraft.font,
                    progressText,
                    x + width - minecraft.font.width(progressText) - 5,
                    downloadButton.y - minecraft.font.lineHeight,
                    CommonColors.GRAY,
                    false
                )
            }
            else {
                val sizeText = Component.literal(DataSizeHelper.getDataSizeString(musicPack.size))
                graphics.text(
                    minecraft.font,
                    sizeText,
                    x + width - minecraft.font.width(sizeText) - 5,
                    downloadButton.y - minecraft.font.lineHeight,
                    CommonColors.GRAY,
                    false
                )
            }

            graphics.textRenderer().acceptScrolling(
                versionText,
                x + 3,
                x + 3,
                downloadButton.x - 3,
                y + 17,
                y + height
            )
        }

        override fun mouseClicked(event: MouseButtonEvent, doubled: Boolean): Boolean {
            if (!isMouseOver(event.x, event.y)) {
                return false
            }

            downloadButton.mouseClicked(event, doubled)
            setSelected(this)
            onSelectPack(musicPack)

            return true
        }

        override fun getNarration(): Component {
            return Component.empty()
        }
    }
}