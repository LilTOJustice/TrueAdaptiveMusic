package liltojustice.trueadaptivemusic.client.gui.widget

import liltojustice.trueadaptivemusic.client.music.MusicPack
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicateTree
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.text.Text
import net.minecraft.util.Colors

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
    private var selected: Selected? = null
    private var mouseButtonHeld = false

    init {
        initPredicateWidgets()
    }

    fun initPredicateWidgets() {
        clearWidgets()
        var row = 0
        musicPack.rules.traverse(
            { node, path ->
                addWidget(
                    ClickableTextWidget(
                        node.predicate.getTypeName(),
                        onClick = { widget ->
                            if (isMovingNode() && node.parent != null) {
                                node.parent!!.adoptChild(selected!!.node!!, node.parent!!.children.indexOf(node))
                                return@ClickableTextWidget
                            }
                            else {
                                onSelectEditExistingNode(node)
                            }
                            selected = Selected(node, widget)
                        },
                        isSelected = { widget -> widget === selected?.widget }),
                    row++,
                    (path.size - 1) * INDENT)
            },
            { node, path ->
                addWidget(
                    ClickableTextWidget(
                        "+ Add",
                        onClick = { widget ->
                            if (isMovingNode()) {
                                node.adoptChild(selected!!.node!!)
                            }
                            else {
                                onSelectCreateNewNode(node)
                            }
                            selected = Selected(null, widget)
                        },
                        isSelected = { widget -> widget === selected?.widget }),
                    row++,
                    path.size * INDENT)
            })
    }

    override fun appendClickableNarrations(builder: NarrationMessageBuilder?) {
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        val result = super.mouseClicked(mouseX, mouseY, button)
        mouseButtonHeld = false
        forEachChild { child ->
            if (child is ClickableTextWidget && child.isMouseOver(mouseX, mouseY)) {
                mouseButtonHeld = true
                return@forEachChild
            }
        }

        return result
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        super.mouseReleased(mouseX, mouseY, button)
        forEachChild { child ->
            if (selected?.widget !== child && child is ClickableTextWidget && child.isMouseOver(mouseX, mouseY)) {
                child.onClick(mouseX, mouseY)
                initPredicateWidgets()
                return@forEachChild
            }
        }

        mouseButtonHeld = false

        return true
    }

    override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(context, mouseX, mouseY, delta)

        if (!isMovingNode()) {
            return
        }

        forEachChild { child ->
            if (child is ClickableTextWidget && child.isMouseOver(mouseX.toDouble(), mouseY.toDouble())) {
                context?.drawText(
                    textRenderer,
                    ARROW_TEXT,
                    child.x - textRenderer.getWidth(ARROW_TEXT) - 2,
                    child.y - (getRowHeight(textRenderer.fontHeight) / 2).toInt(),
                    Colors.WHITE,
                    false)
                return@forEachChild
            }
        }
    }

    private fun isMovingNode(): Boolean {
        return mouseButtonHeld && selected?.node != null
    }

    data class Selected(val node: MusicPredicateTree.Node?, val widget: ClickableTextWidget)

    companion object {
        const val INDENT = 10
        val ARROW_TEXT: Text = Text.literal("->")
    }
}