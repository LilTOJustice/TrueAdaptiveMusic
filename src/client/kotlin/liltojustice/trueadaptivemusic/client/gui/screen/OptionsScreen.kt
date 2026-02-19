package liltojustice.trueadaptivemusic.client.gui.screen

import liltojustice.trueadaptivemusic.client.TAMClient
import liltojustice.trueadaptivemusic.client.gui.widget.OptionsViewWidget
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.screen.ScreenTexts
import net.minecraft.text.Text

@Environment(EnvType.CLIENT)
class OptionsScreen(private val parent: Screen): Screen(
    Text.translatableWithFallback("trueadaptivemusic.options_title", "TrueAdaptiveMusic Options")) {
    private lateinit var optionsViewWidget: OptionsViewWidget
    private lateinit var doneButton: ButtonWidget

    override fun init() {
        optionsViewWidget = OptionsViewWidget(
            TAMClient.options,
            width - BUFFER,
            height - BUFFER - TITLE_Y - textRenderer.fontHeight - 20,
            BUFFER / 2,
            BUFFER / 2 + TITLE_Y + textRenderer.fontHeight)

        doneButton = ButtonWidget.Builder(ScreenTexts.DONE) { close() }
            .width(textRenderer.getWidth(ScreenTexts.DONE) + 10)
            .build()

        doneButton.x = width - doneButton.width
        doneButton.y = height - doneButton.height - 2

        addDrawableChild(optionsViewWidget)
        addDrawableChild(doneButton)
    }

    override fun close() {
        TAMClient.options = optionsViewWidget.getCurrentOptions()
        TAMClient.resetSound()
        client?.setScreen(parent)
    }

    override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        renderBackground(context, mouseX, mouseY, delta)
        context?.drawCenteredTextWithShadow(
            this.textRenderer, this.title, this.width / 2, TITLE_Y, 16777215)
        super.render(context, mouseX, mouseY, delta)
    }

    companion object {
        private const val BUFFER = 6
        private const val TITLE_Y = 8
    }
}