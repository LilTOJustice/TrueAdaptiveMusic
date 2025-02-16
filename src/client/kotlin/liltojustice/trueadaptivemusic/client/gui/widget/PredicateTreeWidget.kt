package liltojustice.trueadaptivemusic.client.gui.widget

import liltojustice.trueadaptivemusic.client.MusicPack
import liltojustice.trueadaptivemusic.client.predicate.MusicPredicate
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.util.Identifier

class PredicateTreeWidget(
    parentScreen: Screen,
    width: Int,
    height: Int,
    private val onSelectPredicate: (predicate: MusicPredicate) -> Unit = {},
    musicPack: MusicPack? = null,
    x: Int = 0,
    y: Int = 0)
    : ContainerWidget(parentScreen, width, height, "Pack Structure", true, x, y) {
     private var selectedWidget: ClickableTextWidget? = null

    init {
        var row = 0
        musicPack?.rules?.traverse(
            { node, depth ->
                addWidget(
                    ClickableTextWidget(
                        node.predicate.getTypeName(),
                        onClick = { widget ->
                            selectedWidget = widget
                            onSelectPredicate(node.predicate) },
                        isSelected = { widget -> widget === selectedWidget}),
                    row++,
                    depth * INDENT)
            },
            { node, depth ->
                addWidget(
                    ClickableTextWidget("+ Add", onClick = {
                        node.newChild("dimension", Identifier("minecraft:overworld"))
                        reinitializeScreen()
                    }),
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