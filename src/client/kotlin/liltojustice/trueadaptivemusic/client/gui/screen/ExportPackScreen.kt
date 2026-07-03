package liltojustice.trueadaptivemusic.client.gui.screen

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import liltojustice.trueadaptivemusic.Reference
import liltojustice.trueadaptivemusic.client.TAMClient
import liltojustice.trueadaptivemusic.client.music.pack.MusicPack
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Colors
import kotlin.coroutines.EmptyCoroutineContext

@Environment(EnvType.CLIENT)
class ExportPackScreen(
    private val musicPack: MusicPack, private val destination: Screen): Screen(Text.literal("")) {
    private val backgroundScope = CoroutineScope(EmptyCoroutineContext)
    private val progress = Reference(0.0)
    private var done = false
    init {
        backgroundScope.launch {
            try {
                TAMClient.musicPack = null
                val path = musicPack.save(progress)
                TAMClient.musicPack = MusicPack.fromFile(path)
                if (destination is MainScreen) {
                    destination.reload()
                }
            }
            catch (e: Exception) {
                TAMClient.errorToast(
                    Component.literal("Failed to export pack."), e.message)
            }
            finally {
                done = true
            }
        }
    }

    override fun close() {
    }

    override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        if (done) {
            client?.setScreen(destination)

            return
        }

        super.render(context, mouseX, mouseY, delta)
        context?.drawCenteredTextWithShadow(
            client?.textRenderer,
            EXPORTING_TEXT,
            width / 2,
            height / 2,
            Colors.WHITE
        )

        val start = 10
        val end = width - 10
        val progressBarY = height / 2 + (client?.textRenderer?.fontHeight ?: 0)
        context?.drawHorizontalLine(start, end, progressBarY, Colors.GRAY)
        context?.drawHorizontalLine(start, (end * progress.value).toInt(), progressBarY, Colors.WHITE)
    }

    companion object {
        private val EXPORTING_TEXT: MutableText = Text.translatableWithFallback(
            "trueadaptivemusic.exporting", "Exporting Pack...")
    }
}