package liltojustice.trueadaptivemusic.client.gui.screen

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import liltojustice.trueadaptivemusic.Reference
import liltojustice.trueadaptivemusic.client.TAMClient
import liltojustice.trueadaptivemusic.client.music.pack.MusicPack
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.util.CommonColors
import kotlin.coroutines.EmptyCoroutineContext

@Environment(EnvType.CLIENT)
class ExportPackScreen(
    private val musicPack: MusicPack, private val destination: Screen): Screen(Component.literal("")) {
    private val backgroundScope = CoroutineScope(EmptyCoroutineContext)
    private val progress = Reference(0.0)
    private var done = false
    init {
        backgroundScope.launch {
            TAMClient.musicPack = null
            val path = musicPack.save(progress)
            TAMClient.musicPack = MusicPack.fromFile(path)
            if (destination is MainScreen) {
                destination.reload()
            }

            done = true
        }
    }

    override fun onClose() {
    }

    override fun extractRenderState(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, a: Float) {
        if (done) {
            minecraft.setScreen(destination)

            return
        }

        super.extractRenderState(graphics, mouseX, mouseY, a)
        graphics.centeredText(font, EXPORTING_TEXT, width / 2, height / 2, CommonColors.WHITE)

        val start = 10
        val end = width - 10
        val progressBarY = height / 2 + font.lineHeight
        graphics.horizontalLine(start, end, progressBarY, CommonColors.GRAY)
        graphics.horizontalLine(
            start, (end * progress.value).toInt(), progressBarY, CommonColors.WHITE)
    }

    companion object {
        private val EXPORTING_TEXT: MutableComponent = Component.translatableWithFallback(
            "trueadaptivemusic.exporting", "Exporting Pack...")
    }
}