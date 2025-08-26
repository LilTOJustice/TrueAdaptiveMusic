package liltojustice.trueadaptivemusic.client.gui.widget

import liltojustice.trueadaptivemusic.Constants
import liltojustice.trueadaptivemusic.client.gui.extensions.getTriggerTooltipString
import liltojustice.trueadaptivemusic.client.gui.widget.utility.ClickableTextWidget
import liltojustice.trueadaptivemusic.client.gui.widget.utility.ContainerWidget
import liltojustice.trueadaptivemusic.client.music.MusicPack
import liltojustice.trueadaptivemusic.client.trigger.event.ErrorEvent
import liltojustice.trueadaptivemusic.client.trigger.predicate.ErrorPredicate
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicateTree
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.tooltip.Tooltip
import net.minecraft.text.Text
import net.minecraft.util.Colors

class PackStructureWidget(
    width: Int,
    height: Int,
    private val musicPack: MusicPack,
    private val onSelectEditExistingNode: (node: MusicPredicateTree.Node) -> Unit,
    private val onSelectCreateNewNode: (parent: MusicPredicateTree.Node) -> Unit,
    x: Int = 0,
    y: Int = 0)
    : ContainerWidget(
    width,
    height,
    Text.translatableWithFallback("trueadaptivemusic.pack_structure", "Pack Structure").string,
    true,
    false,
    true,
    true,
    x,
    y) {
    private var selectedWidget: NodeWidget? = null
    private var mouseButtonHeld = false
    private val selectedNode
        get() = selectedWidget?.targetNode?.let { if (it.isParent) null else it.node }

    init {
        initPredicateWidgets()
    }

    fun initPredicateWidgets() {
        clearWidgets()
        var row = 0
        musicPack.rules.traverse(
            { node, path ->
                val newWidget = addWidget(
                    NodeWidget(
                        node.predicate.getTypeName(),
                        onClick = { widget ->
                            if (selectedWidget === widget) {
                                return@NodeWidget
                            }

                            onSelectEditExistingNode(node)
                            selectedWidget = widget as NodeWidget
                        },
                        isSelected = { widget -> widget === selectedWidget })
                        .withCustomData(TargetNode(node, false)),
                    row++,
                    (path.size - 1) * INDENT) as NodeWidget

                if (node.predicate is ErrorPredicate) {
                    newWidget.color = Colors.RED
                }
                else if (node.events.any { event -> event is ErrorEvent }) {
                    newWidget.color = Constants.Colors.YELLOW
                }

                if (newWidget.targetNode.node === selectedNode) {
                    selectedWidget = newWidget
                }
            },
            { node, path ->
                if (node.predicate is ErrorPredicate) {
                    return@traverse
                }

                addWidget(
                    NodeWidget(
                        "+ ${Text.translatableWithFallback("trueadaptivemusic.add", "Add").string}",
                        onClick = { widget ->
                            if (selectedWidget === widget) {
                                return@NodeWidget
                            }

                            onSelectCreateNewNode(node)
                            selectedWidget = widget as NodeWidget
                        },
                        isSelected = { widget -> widget === selectedWidget })
                        .withCustomData(TargetNode(node, true)),
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
        val result = super.mouseReleased(mouseX, mouseY, button)

        if (!isMovingNode()) {
            return result
        }

        forEachChild { child ->
            if (child === selectedWidget
                || !child.isMouseOver(mouseX, mouseY)
                || child !is NodeWidget
                || selectedNode?.let { child.isValidDestination(it) } != true) {
                return@forEachChild
            }

            val targetNode = child.targetNode.node

            if (child.targetNode.isParent) {
                targetNode.adoptChild(selectedNode!!)
            }
            else {
                targetNode.parent!!
                    .adoptChild(selectedNode!!, targetNode.parent!!.children.indexOf(targetNode))
            }

            initPredicateWidgets()
        }

        mouseButtonHeld = false

        return result
    }

    override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        forEachChild { child ->
            if (child !is NodeWidget) {
                return@forEachChild
            }

            val baseTooltipText = child.getBaseTooltipString()
            child.tooltip =
                if (selectedWidget === child && !child.targetNode.isParent && child.targetNode.node.parent != null)
                    if (baseTooltipText.isBlank())
                        Tooltip.of(Text.literal(MOVE_NODE_STRING))
                    else
                        Tooltip.of(Text.literal("$MOVE_NODE_STRING\n$baseTooltipText"))
                else
                    Tooltip.of(Text.literal(baseTooltipText))
        }

        super.render(context, mouseX, mouseY, delta)

        if (!isMovingNode()) {
            return
        }

        forEachChild { child ->
            if (child === selectedWidget
                || !child.isMouseOver(mouseX.toDouble(), mouseY.toDouble())
                || child !is NodeWidget
            ) {
                return@forEachChild
            }

            val valid = selectedNode?.let { child.isValidDestination(it) } == true

            context?.drawText(
                textRenderer,
                ARROW_TEXT,
                child.x - textRenderer.getWidth(ARROW_TEXT) - 2,
                child.y - (getRowHeight(textRenderer.fontHeight) / 2).toInt(),
                if (valid) Colors.WHITE else Colors.RED,
                false)
            return@forEachChild
        }
    }

    private fun isMovingNode(): Boolean {
        return mouseButtonHeld && selectedNode != null
    }

    companion object {
        const val INDENT = 10
        val MOVE_NODE_STRING: String = Text.translatableWithFallback(
            "trueadaptivemusic.move_node", "Click and drag to move").string
        val ARROW_TEXT: Text = Text.literal("->")
    }

    class NodeWidget(text: String, onClick: (ClickableTextWidget) -> Unit, isSelected: (ClickableTextWidget) -> Boolean)
        : ClickableTextWidget(text, onClick = onClick, isSelected = isSelected)
    {
        val targetNode
            get() = customData as TargetNode

        fun getBaseTooltipString(): String {
            if (targetNode.isParent) {
                return Text.translatableWithFallback("trueadaptivemusic.create_node", "Create a new node")
                    .string
            }

            return targetNode.node.predicate.getTriggerTooltipString() +
                    if (targetNode.node.events.any { event -> event is ErrorEvent })
                        "\n\n${Text.translatableWithFallback(
                            "trueadaptivemusic.has_event_errors",
                            "Has event errors. Click to see.").string}"
                    else
                        ""
        }

        fun isValidDestination(selectedNode: MusicPredicateTree.Node): Boolean {
            return (targetNode.node.parent != null || targetNode.isParent)
                    && targetNode.node.isValidNewChild(selectedNode)
        }
    }

    data class TargetNode(val node: MusicPredicateTree.Node, val isParent: Boolean)
}