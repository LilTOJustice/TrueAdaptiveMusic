package liltojustice.trueadaptivemusic.client.gui.widget

import liltojustice.trueadaptivemusic.client.predicate.MusicPredicate
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder

class PredicateViewWidget(parentScreen: Screen, width: Int, height: Int, x: Int = 0, y: Int = 0)
    : ContainerWidget(parentScreen, width, height, "Predicate View", true, x, y) {
    private var selectedPredicate: MusicPredicate? = null

    override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(context, mouseX, mouseY, delta)
        drawCenteredText(
            context,
            selectedPredicate?.getTypeName() ?: "Select a predicate",
            0,
            width / 2)
    }

    override fun appendClickableNarrations(builder: NarrationMessageBuilder?) {
    }

    fun setPredicate(predicate: MusicPredicate) {
        selectedPredicate = predicate
    }
}

