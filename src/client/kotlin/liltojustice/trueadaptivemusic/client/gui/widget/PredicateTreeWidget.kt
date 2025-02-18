package liltojustice.trueadaptivemusic.client.gui.widget

import liltojustice.trueadaptivemusic.client.MusicPack
import liltojustice.trueadaptivemusic.client.predicate.MusicPredicate
import liltojustice.trueadaptivemusic.client.predicate.MusicPredicateTree
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder

class PredicateTreeWidget(
    width: Int,
    height: Int,
    private val onSelectExistingPredicate: (predicate: MusicPredicate) -> Unit = {},
    private val onRequestCreateNewPredicate: (parent: MusicPredicateTree.Node) -> Unit = {},
    private val musicPack: MusicPack? = null,
    x: Int = 0,
    y: Int = 0)
    : ContainerWidget(width, height, "Pack Structure", true, false, x, y) {
    private var selectedWidget: ClickableTextWidget? = null

    init {
        initPredicateWidgets()
    }

    private fun initPredicateWidgets() {
        clearWidgets()
        var row = 0
        musicPack?.rules?.traverse(
            { node, depth ->
                addWidget(
                    ClickableTextWidget(
                        node.predicate.getTypeName(),
                        onClick = { widget ->
                            selectedWidget = widget
                            onSelectExistingPredicate(node.predicate) },
                        isSelected = { widget -> widget === selectedWidget}),
                    row++,
                    depth * INDENT)
            },
            { node, depth ->
                addWidget(
                    ClickableTextWidget("+ Add",
                        onClick = { widget ->
                            selectedWidget = widget
                            //node.newChild("dimension", Identifier("minecraft:overworld"))
                            //musicPack.initRules()
                            //initPredicateWidgets()
                            onRequestCreateNewPredicate(node) },
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