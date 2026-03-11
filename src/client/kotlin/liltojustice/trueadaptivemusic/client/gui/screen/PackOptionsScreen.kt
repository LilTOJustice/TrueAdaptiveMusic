package liltojustice.trueadaptivemusic.client.gui.screen

import liltojustice.trueadaptivemusic.client.gui.widget.PackOptionsViewWidget
import liltojustice.trueadaptivemusic.client.music.pack.MusicPack
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.screen.ScreenTexts
import net.minecraft.text.Text
import net.minecraft.util.Colors

@Environment(EnvType.CLIENT)
class PackOptionsScreen(private val parent: Screen, private val musicPack: MusicPack): Screen(
    Text.translatableWithFallback("trueadaptivemusic.pack_options_title", "Edit Pack Options")) {
    private lateinit var packOptionsViewWidget: PackOptionsViewWidget
    private lateinit var doneButton: ButtonWidget

    override fun init() {
        packOptionsViewWidget = PackOptionsViewWidget(
            musicPack.options,
            width - BUFFER,
            height - BUFFER - TITLE_Y - textRenderer.fontHeight - 20,
            BUFFER / 2,
            BUFFER / 2 + TITLE_Y + textRenderer.fontHeight)

        doneButton = ButtonWidget.Builder(ScreenTexts.DONE) { close() }
            .width(textRenderer.getWidth(ScreenTexts.DONE) + 10)
            .build()

        doneButton.x = width - doneButton.width
        doneButton.y = packOptionsViewWidget.y + packOptionsViewWidget.height + 2

        addDrawableChild(packOptionsViewWidget)
        addDrawableChild(doneButton)
    }

    override fun close() {
        musicPack.options = packOptionsViewWidget.getCurrentOptions()
        musicPack.initOptions()
        client?.setScreen(parent)
    }

    override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        context?.drawCenteredTextWithShadow(
            this.textRenderer, this.title, this.width / 2, TITLE_Y, Colors.WHITE)
        super.render(context, mouseX, mouseY, delta)
    }

    companion object {
        private const val BUFFER = 6
        private const val TITLE_Y = 8
    }
}