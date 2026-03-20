package liltojustice.trueadaptivemusic.client.gui.screen

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
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
class ExportPackScreen(private val musicPack: MusicPack, destination: Screen): Screen(Text.literal("")) {
    private val backgroundScope = CoroutineScope(EmptyCoroutineContext)
    init {
        backgroundScope.launch {
            TAMClient.musicPack = null
            val path = musicPack.save()
            TAMClient.musicPack = MusicPack.fromFile(path)
            if (destination is MainScreen) {
                destination.reload()
            }

            client?.setScreen(destination)
        }
    }

    override fun close() {
    }

    override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(context, mouseX, mouseY, delta)
        context?.drawCenteredTextWithShadow(
            client?.textRenderer,
            EXPORTING_TEXT,
            width / 2,
            height / 2,
            Colors.WHITE
        )
    }

    companion object {
        private val EXPORTING_TEXT: MutableText = Text.translatableWithFallback(
            "trueadaptivemusic.exporting", "Exporting Pack...")
    }
}