package liltojustice.trueadaptivemusic.client.gui.widget

import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.text.Text

class EmptyClickableWidget(): ClickableWidget(0, 0, 0, 0, Text.literal("")) {
    override fun renderButton(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
    }

    override fun appendClickableNarrations(builder: NarrationMessageBuilder?) {
    }
}