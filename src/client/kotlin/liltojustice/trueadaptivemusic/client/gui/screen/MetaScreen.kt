package liltojustice.trueadaptivemusic.client.gui.screen

import liltojustice.trueadaptivemusic.client.gui.widget.MetaViewWidget
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
class MetaScreen(private val parent: Screen, private val musicPack: MusicPack): Screen(
    Text.translatableWithFallback("trueadaptivemusic.meta_title", "Edit Pack Meta")) {
    private lateinit var metaViewWidget: MetaViewWidget
    private lateinit var doneButton: ButtonWidget

    override fun init() {
        metaViewWidget = MetaViewWidget(
            musicPack.metadata,
            width - BUFFER,
            height - BUFFER - TITLE_Y - textRenderer.fontHeight - 20,
            BUFFER / 2,
            BUFFER / 2 + TITLE_Y + textRenderer.fontHeight)

        doneButton = ButtonWidget.Builder(ScreenTexts.DONE) { close() }
            .width(textRenderer.getWidth(ScreenTexts.DONE) + 10)
            .build()

        doneButton.x = width - doneButton.width
        doneButton.y = metaViewWidget.y + metaViewWidget.height + 2

        addDrawableChild(metaViewWidget)
        addDrawableChild(doneButton)
    }

    override fun close() {
        musicPack.metadata = metaViewWidget.getCurrentMeta()
        musicPack.initMeta()
        client?.setScreen(parent)
    }

    override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        renderBackground(context)
        context?.drawCenteredTextWithShadow(
            this.textRenderer, this.title, this.width / 2, TITLE_Y, Colors.WHITE)
        super.render(context, mouseX, mouseY, delta)
    }

    companion object {
        private const val BUFFER = 6
        private const val TITLE_Y = 8
    }
}