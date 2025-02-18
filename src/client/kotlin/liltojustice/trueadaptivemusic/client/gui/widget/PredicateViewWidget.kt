package liltojustice.trueadaptivemusic.client.gui.widget

import liltojustice.trueadaptivemusic.client.predicate.MusicPredicate
import liltojustice.trueadaptivemusic.client.predicate.MusicPredicateTree
import liltojustice.trueadaptivemusic.client.predicate.RootPredicate
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder

class PredicateViewWidget(width: Int, height: Int, x: Int = 0, y: Int = 0)
    : ContainerWidget(width, height, "Predicate View", true, false, x, y) {
    private var selectedPredicate: MusicPredicate? = null
    private var newPredicateParent: MusicPredicateTree.Node? = null
    private var selectedNewPredicateTypeName: String? = null
    private val predicateTypeNameOptions = MusicPredicate.getTypeNames()
        .filter { typeName -> typeName != RootPredicate.getTypeName() }
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

    fun setEditPredicate(predicate: MusicPredicate) {
        clearWidgetsFromRender()
        selectedPredicate = predicate
        newPredicateMode = false
    }

    fun unsetEditPredicate() {
        clearWidgetsFromRender()
        selectedPredicate = null
        newPredicateMode = false
    }

    fun setCreateNewPredicate(parent: MusicPredicateTree.Node) {
        clearWidgetsFromRender()
        newPredicateParent = parent
        newPredicateMode = true
    }

    private fun renderNewPredicateMode(context: DrawContext?) {
        drawCenteredText(
            context,
            "New Predicate",
            0,
            width / 2)
        addWidgetFromRender(
            {
                DropdownWidget(
                    predicateTypeNameOptions,
                    { typeName ->  selectedNewPredicateTypeName = typeName})
            },
            "predicateTypeChoice",
            1)
    }

    private fun renderEditPredicateMode(context: DrawContext?) {
        val selectedPredicate = selectedPredicate!!
        drawCenteredText(
            context,
            selectedPredicate.getTypeName(),
            0,
            width / 2)
    }

    override fun appendClickableNarrations(builder: NarrationMessageBuilder?) {
    }
}

