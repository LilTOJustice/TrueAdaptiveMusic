package liltojustice.trueadaptivemusic.client.gui.screen

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import liltojustice.trueadaptivemusic.Reference
import liltojustice.trueadaptivemusic.client.TAMClient
import liltojustice.trueadaptivemusic.client.music.pack.MusicPack
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Component
import net.minecraft.util.CommonColors
import kotlin.coroutines.EmptyCoroutineContext

class ExportPackScreen(
    private val musicPack: MusicPack, private val destination: Screen
): Screen(Component.literal("")) {
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

    override fun onClose() {
    }

    override fun render(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        if (done) {
            minecraft!!.setScreen(destination)

            return
        }

        super.render(context, mouseX, mouseY, delta)
        context.drawCenteredString(
            minecraft!!.font,
            EXPORTING_TEXT,
            width / 2,
            height / 2,
            CommonColors.WHITE
        )

        val start = 10
        val end = width - 10
        val progressBarY = height / 2 + minecraft!!.font.lineHeight
        context.hLine(start, end, progressBarY, CommonColors.GRAY)
        context.hLine(start, (end * progress.value).toInt(), progressBarY, CommonColors.WHITE)
    }

    companion object {
        private val EXPORTING_TEXT: MutableComponent = Component.translatableWithFallback(
            "trueadaptivemusic.exporting", "Exporting Pack...")
    }
}