package liltojustice.trueadaptivemusic.client.gui.screen

import liltojustice.trueadaptivemusic.client.gui.widget.PackOptionsViewWidget
import liltojustice.trueadaptivemusic.client.music.pack.MusicPack
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.components.Button
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import net.minecraft.util.CommonColors

class PackOptionsScreen(private val parent: Screen, private val musicPack: MusicPack): Screen(
    Component.translatableWithFallback("trueadaptivemusic.pack_options_title", "Edit Pack Options")) {
    private lateinit var packOptionsViewWidget: PackOptionsViewWidget
    private lateinit var doneButton: Button

    override fun init() {
        packOptionsViewWidget = PackOptionsViewWidget(
            musicPack.options,
            width - BUFFER,
            height - BUFFER - TITLE_Y - font.lineHeight - 20,
            BUFFER / 2,
            BUFFER / 2 + TITLE_Y + font.lineHeight
        )

        doneButton = Button.Builder(CommonComponents.GUI_DONE) { onClose() }
            .width(font.width(CommonComponents.GUI_DONE) + 10)
            .build()

        doneButton.x = width - doneButton.width
        doneButton.y = packOptionsViewWidget.y + packOptionsViewWidget.height + 2

        addRenderableWidget(packOptionsViewWidget)
        addRenderableWidget(doneButton)
    }

    override fun onClose() {
        musicPack.options = packOptionsViewWidget.getCurrentOptions()
        musicPack.initOptions()
        minecraft.setScreen(parent)
    }

    override fun render(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        context.drawCenteredString(
            this.font, this.title, this.width / 2, TITLE_Y, CommonColors.WHITE)
        super.render(context, mouseX, mouseY, delta)
    }

    companion object {
        private const val BUFFER = 6
        private const val TITLE_Y = 8
    }
}