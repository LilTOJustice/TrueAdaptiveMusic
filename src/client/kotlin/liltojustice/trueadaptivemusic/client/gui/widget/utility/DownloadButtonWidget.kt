package liltojustice.trueadaptivemusic.client.gui.widget.utility

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import liltojustice.trueadaptivemusic.Logger
import liltojustice.trueadaptivemusic.client.gui.RenderState
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.LoadingDisplay
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Colors
import net.minecraft.util.Util
import kotlin.coroutines.EmptyCoroutineContext

class DownloadButtonWidget(
    downloaded: Boolean,
    private val downloadAction: () -> Unit,
): ClickableTextWidget(DOWNLOAD_TEXT.string, 0, 0, true) {
    private var downloadStatus: RenderState? = if (downloaded) RenderState.Success else null
    private val backgroundScope = CoroutineScope(EmptyCoroutineContext)

    init {
        onClick = {
            backgroundScope.launch {
                try {
                    downloadStatus = RenderState.Loading
                    downloadAction()
                    downloadStatus = RenderState.Success
                } catch (e: Exception) {
                    Logger.logError("Download failed:\n$e")
                    downloadStatus = RenderState.Failure
                }
            }
        }
    }

    override fun renderWidget(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        active = true
        when (downloadStatus) {
            RenderState.Loading -> {
                val loadingText = LoadingDisplay.get(Util.getMeasuringTimeMs())
                context?.drawTextWithShadow(
                    textRenderer, loadingText, x + width - textRenderer.getWidth(loadingText) - 2, y, Colors.GRAY)
                active = false

                return
            }
            RenderState.Success -> {
                message = DOWNLOADED_TEXT
                active = false
            }
            RenderState.Failure -> {
                message = DOWNLOAD_FAILED_TEXT
            }
            null -> {
                message = DOWNLOAD_TEXT
            }
        }
        width = textRenderer.getWidth(message)

        super.renderWidget(context, mouseX, mouseY, delta)
    }

    companion object {
        private val DOWNLOAD_TEXT: MutableText = Text.translatableWithFallback(
            "trueadaptivemusic.download", "Download")
        private val DOWNLOADED_TEXT: MutableText =
            Text.translatableWithFallback(
                "trueadaptivemusic.downloaded", "Downloaded")
        private val DOWNLOAD_FAILED_TEXT: MutableText =
            Text.translatableWithFallback(
                "trueadaptivemusic.download_failed", "Download Failed")
    }
}