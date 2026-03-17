package liltojustice.trueadaptivemusic.client.gui.widget.utility

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import liltojustice.trueadaptivemusic.client.gui.RenderState
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.widget.LoadingWidget
import net.minecraft.text.MutableText
import kotlin.coroutines.EmptyCoroutineContext

class DownloadButtonWidget(
    private val downloadAction: () -> Unit,
): ClickableTextWidget(DOWNLOAD_TEXT.string, 0, 0, true) {
    private var downloadStatus: RenderState? = null
    private val loadingWidget = LoadingWidget(textRenderer, DOWNLOADING_TEXT)
    private val backgroundScope = CoroutineScope(EmptyCoroutineContext)

    init {
        onClick = {
            backgroundScope.launch {
                try {
                    downloadStatus = RenderState.Loading
                    downloadAction()
                    downloadStatus = RenderState.Success
                } catch (_: Exception) {
                    downloadStatus = RenderState.Failure
                }
            }
        }
    }

    override fun renderWidget(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        when (downloadStatus) {
            RenderState.Loading -> {
                loadingWidget.setPosition(x, y)
                loadingWidget.render(context, mouseX, mouseY, delta)
                active = false

                return
            }
            RenderState.Success -> {
                message = DOWNLOADED_TEXT
                active = false
            }
            RenderState.Failure -> {
                message = DOWNLOAD_FAILED_TEXT
                active = true
            }
            null -> {
                message = DOWNLOAD_TEXT
                active = true
            }
        }

        super.renderWidget(context, mouseX, mouseY, delta)
    }

    companion object {
        private val DOWNLOAD_TEXT: MutableText = net.minecraft.text.Text.translatableWithFallback(
            "trueadaptivemusic.download", "Download")
        private val DOWNLOADING_TEXT: MutableText =
            net.minecraft.text.Text.translatableWithFallback(
                "trueadaptivemusic.downloading_pack", "Downloading pack")
        private val DOWNLOADED_TEXT: MutableText =
            net.minecraft.text.Text.translatableWithFallback(
                "trueadaptivemusic.downloaded", "Downloaded")
        private val DOWNLOAD_FAILED_TEXT: MutableText =
            net.minecraft.text.Text.translatableWithFallback(
                "trueadaptivemusic.download_failed", "Download Failed")
    }
}