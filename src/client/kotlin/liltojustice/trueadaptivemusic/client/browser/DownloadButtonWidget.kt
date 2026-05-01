package liltojustice.trueadaptivemusic.client.browser

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import liltojustice.trueadaptivemusic.Logger
import liltojustice.trueadaptivemusic.Reference
import liltojustice.trueadaptivemusic.client.gui.RenderState
import liltojustice.trueadaptivemusic.client.gui.widget.utility.ClickableTextWidget
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.LoadingDisplay
import net.minecraft.client.gui.tooltip.Tooltip
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Colors
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

    override fun renderWidget(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        active = true
        setTooltip(null)
        when (downloadStatus) {
            RenderState.Loading -> {
                val loadingText = LoadingDisplay.get(Util.getMeasuringTimeMs())
                context?.drawTextWithShadow(
                    textRenderer, loadingText, x + (width - textRenderer.getWidth(loadingText)) / 2, y, Colors.GRAY)
                active = false
                progress?.value?.let {
                    context?.drawHorizontalLine(x, x + width, y + height, Colors.GRAY)
                    context?.drawHorizontalLine(x, (x + width * it).toInt(), y + height, Colors.WHITE)
                }

                return
            }
            RenderState.Success -> {
                message = DOWNLOADED_TEXT
                active = false
            }
            RenderState.Failure -> {
                message = if (isUpdate) UPDATE_FAILED_TEXT else DOWNLOAD_FAILED_TEXT
                lastException?.message?.let { setTooltip(Tooltip.of(Text.literal(it))) }
            }
            null -> {
                message = if (isUpdate) UPDATE_TEXT else DOWNLOAD_TEXT
            }
        }
        width = textRenderer.getWidth(message)

        super.renderWidget(context, mouseX, mouseY, delta)
    }

    companion object {
        private val DOWNLOAD_TEXT: MutableText = Text.translatableWithFallback(
            "trueadaptivemusic.download", "Download")
        private val UPDATE_TEXT: MutableText = Text.translatableWithFallback(
            "trueadaptivemusic.update", "Update")
        private val DOWNLOADED_TEXT: MutableText = Text.translatableWithFallback(
            "trueadaptivemusic.downloaded", "Downloaded")
        private val DOWNLOAD_FAILED_TEXT: MutableText = Text.translatableWithFallback(
            "trueadaptivemusic.download_failed", "Download Failed")
        private val UPDATE_FAILED_TEXT: MutableText = Text.translatableWithFallback(
            "trueadaptivemusic.update_failed", "Update Failed")
    }
}