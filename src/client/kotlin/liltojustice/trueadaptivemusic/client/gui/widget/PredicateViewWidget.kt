package liltojustice.trueadaptivemusic.client.gui.widget

import liltojustice.trueadaptivemusic.client.predicate.MusicPredicate
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder

class PredicateViewWidget(width: Int, height: Int, showHeader: Boolean = false, x: Int = 0, y: Int = 0)
    : ContainerWidget(width, height, "Predicate View", showHeader, x, y) {
    private var selectedPredicate: MusicPredicate? = null

    override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(context, mouseX, mouseY, delta)
        val textRenderer = MinecraftClient.getInstance().textRenderer
        drawCenteredText(context, textRenderer, selectedPredicate?.getTypeName() ?: "Select a predicate", 0)
    }

    override fun appendClickableNarrations(builder: NarrationMessageBuilder?) {
    }

    fun setPredicate(predicate: MusicPredicate) {
        selectedPredicate = predicate
    }

    fun MusicPredicate.getTypeName(): String {
        return this.getTypeName()
    }
}

