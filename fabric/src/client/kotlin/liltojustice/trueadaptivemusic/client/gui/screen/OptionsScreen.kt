package liltojustice.trueadaptivemusic.client.gui.screen

import liltojustice.trueadaptivemusic.client.TAMClient
import liltojustice.trueadaptivemusic.client.gui.widget.OptionsViewWidget
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import net.minecraft.util.CommonColors

@Environment(EnvType.CLIENT)
class OptionsScreen(private val parent: Screen): Screen(
    Component.translatableWithFallback("trueadaptivemusic.options_title", "TrueAdaptiveMusic Options")) {
    private lateinit var optionsViewWidget: OptionsViewWidget
    private lateinit var doneButton: Button

    override fun init() {
        optionsViewWidget = OptionsViewWidget(
            TAMClient.options,
            width - BUFFER,
            height - BUFFER - TITLE_Y - font.lineHeight - 20,
            BUFFER / 2,
            BUFFER / 2 + TITLE_Y + font.lineHeight
        )

        doneButton = Button.Builder(CommonComponents.GUI_DONE) { onClose() }
            .width(font.width(CommonComponents.GUI_DONE) + 10)
            .build()

        doneButton.x = width - doneButton.width
        doneButton.y = height - doneButton.height - 2

        addRenderableWidget(optionsViewWidget)
        addRenderableWidget(doneButton)
    }

    override fun onClose() {
        TAMClient.options = optionsViewWidget.getCurrentOptions()
        TAMClient.resetSound()
        minecraft.gui.setScreen(parent)
    }

    override fun extractRenderState(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, a: Float) {
        graphics.centeredText(font, title, width / 2, TITLE_Y, CommonColors.WHITE)
        super.extractRenderState(graphics, mouseX, mouseY, a)
    }

    companion object {
        private const val BUFFER = 6
        private const val TITLE_Y = 8
    }
}