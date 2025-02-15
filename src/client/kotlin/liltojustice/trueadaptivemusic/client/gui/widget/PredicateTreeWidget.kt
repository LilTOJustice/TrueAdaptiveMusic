package liltojustice.trueadaptivemusic.client.gui.widget

import liltojustice.trueadaptivemusic.client.predicate.MusicPredicate
import liltojustice.trueadaptivemusic.client.predicate.MusicPredicateTree
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder

class PredicateTreeWidget(
    width: Int,
    height: Int,
    private val onSelectPredicate: (predicate: MusicPredicate) -> Unit = {},
    musicPredicateTree: MusicPredicateTree? = null,
    x: Int = 0,
    y: Int = 0)
    : ContainerWidget(width, height, "Pack Structure", true, x, y) {
     private var selectedWidget: ClickableTextWidget? = null

    init {
        var row = 0
        musicPredicateTree?.preorderTraverse { node, depth ->
            addWidget(
                ClickableTextWidget(
                    node.predicate.getTypeName(),
                    onClick = { widget ->
                        selectedWidget = widget
                        onSelectPredicate(node.predicate) },
                    isSelected = { widget -> widget === selectedWidget}),
                row++,
                depth * 5)
        }
    }

    override fun appendClickableNarrations(builder: NarrationMessageBuilder?) {
    }


}