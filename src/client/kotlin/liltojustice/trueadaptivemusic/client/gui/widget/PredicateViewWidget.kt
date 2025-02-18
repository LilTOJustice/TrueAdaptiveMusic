package liltojustice.trueadaptivemusic.client.gui.widget

import liltojustice.trueadaptivemusic.client.predicate.MusicPredicate
import liltojustice.trueadaptivemusic.client.predicate.MusicPredicateTree
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder

class PredicateViewWidget(width: Int, height: Int, x: Int = 0, y: Int = 0)
    : ContainerWidget(width, height, "Predicate View", true, false, x, y) {
    private var selectedPredicate: MusicPredicate? = null
    private var newPredicateParent: MusicPredicateTree.Node? = null
    private val newPredicateOptions = MusicPredicate.getTypeNames()
    private var newPredicateMode = false

    override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(context, mouseX, mouseY, delta)
        if (!enabled) {
            return
        }

        if (newPredicateMode) {
            renderNewPredicateMode(context)
        }
        else if (selectedPredicate != null) {
            renderEditPredicateMode(context)
        }
        else {
            drawCenteredText(
                context,
                "Select or add a predicate",
                0,
                width / 2)
        }
    }

    fun renderNewPredicateMode(context: DrawContext?) {
        drawCenteredText(
            context,
            "New Predicate",
            0,
            width / 2)
    }

    fun renderEditPredicateMode(context: DrawContext?) {
        //addWidget(DropdownWidget(100, 100, newPredicateOptions, { typeName -> onSelectNewPredicateType(typeName) }), row + 1)

    }

    override fun appendClickableNarrations(builder: NarrationMessageBuilder?) {
    }

    fun setEditPredicate(predicate: MusicPredicate) {
        selectedPredicate = predicate
        newPredicateMode = false
    }

    fun setCreateNewPredicate(parent: MusicPredicateTree.Node) {
        newPredicateParent = parent
        newPredicateMode = true
    }
}

