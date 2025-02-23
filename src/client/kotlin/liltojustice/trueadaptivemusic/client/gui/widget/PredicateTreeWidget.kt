package liltojustice.trueadaptivemusic.client.gui.widget

import liltojustice.trueadaptivemusic.client.MusicPack
import liltojustice.trueadaptivemusic.client.predicate.MusicPredicateTree
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder

class PredicateTreeWidget(
    width: Int,
    height: Int,
    private val musicPack: MusicPack,
    private val onSelectEditExistingNode: (node: MusicPredicateTree.Node) -> Unit,
    private val onSelectCreateNewNode: (parent: MusicPredicateTree.Node) -> Unit,
    x: Int = 0,
    y: Int = 0)
    : ContainerWidget(
    width, height, "Pack Structure", true, false, true, x, y) {
    private var selectedWidget: ClickableTextWidget? = null

    init {
        initPredicateWidgets()
    }

    fun initPredicateWidgets() {
        clearWidgets()
        var row = 0
        musicPack.rules.traverse(
            { node, depth ->
                addWidget(
                    ClickableTextWidget(
                        node.predicate.getTypeName(),
                        onClick = { widget ->
                            selectedWidget = widget
                            onSelectEditExistingNode(node)
                        },
                        isSelected = { widget -> widget === selectedWidget}),
                    row++,
                    depth * INDENT)
            },
            { node, depth ->
                addWidget(
                    ClickableTextWidget("+ Add",
                        onClick = { widget ->
                            selectedWidget = widget
                            onSelectCreateNewNode(node) },
                        isSelected = { widget -> widget === selectedWidget }),
                    row++,
                    (depth + 1) * INDENT)
            })
    }

    override fun appendClickableNarrations(builder: NarrationMessageBuilder?) {
    }

    companion object {
        const val INDENT = 10
    }
}