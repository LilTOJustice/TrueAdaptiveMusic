package liltojustice.trueadaptivemusic.client.gui.widget.utility

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import liltojustice.trueadaptivemusic.Logger
import liltojustice.trueadaptivemusic.Reference
import liltojustice.trueadaptivemusic.client.gui.RenderState
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.Tooltip
import net.minecraft.client.gui.screens.LoadingDotsText
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.util.CommonColors
import net.minecraft.util.Util
import kotlin.coroutines.EmptyCoroutineContext

class DownloadButtonWidget(
    downloaded: Boolean,
    private val isUpdate: Boolean,
    private val progress: Reference<Float>? = null,
    private val downloadAction: () -> Unit,
): ClickableTextWidget("", 0, 0, true) {
    var downloadStatus: RenderState? = if (downloaded) RenderState.Success else null
    private val backgroundScope = CoroutineScope(EmptyCoroutineContext)
    private var lastException: Exception? = null

    init {
        onClick = {
            backgroundScope.launch {
                try {
                    downloadStatus = RenderState.Loading
                    downloadAction()
                    downloadStatus = RenderState.Success
                } catch (e: Exception) {
                    Logger.logError("Download failed:\n$e")
                    lastException = e
                    downloadStatus = RenderState.Failure
                }
            }
        }
    }

    override fun extractWidgetRenderState(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, a: Float) {
        active = true
        setTooltip(null)
        when (downloadStatus) {
            RenderState.Loading -> {
                val loadingText = LoadingDotsText.get(Util.getMillis())
                graphics.text(
                    font,
                    loadingText,
                    x + (width - font.width(loadingText)) / 2,
                    y,
                    CommonColors.GRAY
                )
                active = false
                progress?.value?.let {
                    graphics.horizontalLine(x, x + width, y + height, CommonColors.GRAY)
                    graphics.horizontalLine(
                        x, (x + width * it).toInt(), y + height, CommonColors.WHITE)
                }

                return
            }
            RenderState.Success -> {
                message = DOWNLOADED_TEXT
                active = false
            }
            RenderState.Failure -> {
                message = if (isUpdate) UPDATE_FAILED_TEXT else DOWNLOAD_FAILED_TEXT
                lastException?.message?.let { setTooltip(Tooltip.create(Component.literal(it))) }
            }
            null -> {
                message = if (isUpdate) UPDATE_TEXT else DOWNLOAD_TEXT
            }
        }

        width = font.width(message)

        super.extractWidgetRenderState(graphics, mouseX, mouseY, a)
    }

    companion object {
        private val DOWNLOAD_TEXT: MutableComponent = Component.translatableWithFallback(
            "trueadaptivemusic.download", "Download")
        private val UPDATE_TEXT: MutableComponent = Component.translatableWithFallback(
            "trueadaptivemusic.update", "Update")
        private val DOWNLOADED_TEXT: MutableComponent = Component.translatableWithFallback(
            "trueadaptivemusic.downloaded", "Downloaded")
        private val DOWNLOAD_FAILED_TEXT: MutableComponent = Component.translatableWithFallback(
            "trueadaptivemusic.download_failed", "Download Failed")
        private val UPDATE_FAILED_TEXT: MutableComponent = Component.translatableWithFallback(
            "trueadaptivemusic.update_failed", "Update Failed")
    }
}