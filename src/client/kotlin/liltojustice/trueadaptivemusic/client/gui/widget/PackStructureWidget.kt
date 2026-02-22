package liltojustice.trueadaptivemusic.client.gui.widget

import liltojustice.trueadaptivemusic.client.gui.extensions.getTriggerTooltipString
import liltojustice.trueadaptivemusic.client.gui.widget.utility.ClickableTextWidget
import liltojustice.trueadaptivemusic.client.gui.widget.utility.ContainerWidget
import liltojustice.trueadaptivemusic.client.music.pack.MusicPack
import liltojustice.trueadaptivemusic.client.trigger.event.ErrorEvent
import liltojustice.trueadaptivemusic.client.trigger.predicate.ErrorPredicate
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicateTree
import net.minecraft.client.gui.Click
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.tooltip.Tooltip
import net.minecraft.client.input.KeyInput
import net.minecraft.text.Text
import net.minecraft.util.Colors

class PackStructureWidget(
    width: Int,
    height: Int,
    private val musicPack: MusicPack,
    private val onChangesSaved: () -> Unit,
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
    private var mouseButtonHeld = false
    private var shiftHeld = false
    private var ctrlHeld = false
    private var targetedNode: MusicPredicateTree.Node? = null

    init {
        initPredicateWidgets()
    }

    fun setNode(node: MusicPredicateTree.Node?) {
        targetedNode = node
    }

    fun initPredicateWidgets() {
        clearWidgets()
        var row = 0
        musicPack.rules.traverse(
            { node, path ->
                val newWidget = addWidget(
                    NodeWidget(
                        MusicPredicate.getDisplayName(node.predicate.getTypeName()).string,
                        onClick = { widget ->
                            onSelectEditExistingNode(node)
                        },
                        isSelected = { node === targetedNode })
                        .withCustomData(TargetNode(node, false)),
                    row++,
                    (path.size - 1) * INDENT) as NodeWidget

                if (node.predicate is ErrorPredicate) {
                    newWidget.color = Colors.RED
                }
                else if (node.events.any { event -> event is ErrorEvent }) {
                    newWidget.color = Colors.YELLOW
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
                            onSelectCreateNewNode(node)
                        },
                        isSelected = { false }
                    )
                        .withCustomData(TargetNode(node, true)),
                    row++,
                    path.size * INDENT)
            })
    }

    override fun appendClickableNarrations(builder: NarrationMessageBuilder?) {
    }

    override fun keyPressed(input: KeyInput): Boolean {
        if (input.key == SHIFT_KEY) {
            shiftHeld = true
        }

        if (input.key == CTRL_KEY) {
            ctrlHeld = true
        }

        return super.keyPressed(input)
    }

    override fun keyReleased(input: KeyInput): Boolean {
        if (input.key == SHIFT_KEY) {
            shiftHeld = false
        }

        if (input.key == CTRL_KEY) {
            ctrlHeld = false
        }

        return super.keyReleased(input)
    }

    override fun mouseClicked(click: Click, doubled: Boolean): Boolean {
        val result = super.mouseClicked(click, doubled)
        mouseButtonHeld = false
        forEachChild { child ->
            if (child is ClickableTextWidget && child.isMouseOver(click.x, click.y)) {
                mouseButtonHeld = true
                return@forEachChild
            }
        }

        return result
    }

    override fun mouseReleased(click: Click): Boolean {
        val result = super.mouseReleased(click)

        if (!isMovingNode()) {
            return result
        }

        forEachChild { child ->
            if (child !is NodeWidget
                || !child.isMouseOver(click.x, click.y)
                || (targetedNode === child.targetNode.node && !shiftHeld)
                || targetedNode?.let { child.isValidDestination(it) || shiftHeld } != true) {
                return@forEachChild
            }

            val targetNode = child.targetNode.node
            val toAdopt = targetedNode?.let {
                if (shiftHeld) {
                    it.copy(ctrlHeld)
                }
                else {
                    it
                }
            } ?: return@forEachChild

            if (child.targetNode.isParent) {
                targetNode.adoptChild(toAdopt)
            }
            else {
                targetNode.parent?.let { it.adoptChild(toAdopt, it.children.indexOf(targetNode)) }
            }

            musicPack.initRules()
            onChangesSaved()
            initPredicateWidgets()
        }

        mouseButtonHeld = false

        return result
    }

    override fun renderWidget(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        forEachChild { child ->
            if (child !is NodeWidget) {
                return@forEachChild
            }

            val baseTooltipText = child.getBaseTooltipString()
            child.setTooltip(
                if (targetedNode === child.targetNode.node &&
                    !child.targetNode.isParent &&
                    child.targetNode.node.parent != null)
                    if (baseTooltipText.isBlank())
                        Tooltip.of(Text.literal(MOVE_NODE_STRING))
                    else
                        Tooltip.of(Text.literal("$MOVE_NODE_STRING\n\n$baseTooltipText"))
                else
                    Tooltip.of(Text.literal(baseTooltipText)))
        }

        super.renderWidget(context, mouseX, mouseY, delta)

        if (!isMovingNode()) {
            return
        }

        forEachChild { child ->
            if (child !is NodeWidget
                || !child.isMouseOver(mouseX.toDouble(), mouseY.toDouble())
                || (child.targetNode.node === targetedNode && !shiftHeld)) {
                return@forEachChild
            }

            val valid = targetedNode?.let { child.isValidDestination(it) || shiftHeld } == true

            context?.drawText(
                textRenderer,
                ARROW_TEXT,
                child.x - textRenderer.getWidth(ARROW_TEXT) - 2,
                child.y - (getRowHeight(textRenderer.fontHeight) / 2).toInt(),
                if (valid) Colors.WHITE else Colors.RED,
                false
            )
            return@forEachChild
        }
    }

    private fun isMovingNode(): Boolean {
        return mouseButtonHeld && targetedNode != null
    }

    companion object {
        const val INDENT = 10
        const val SHIFT_KEY = 340
        const val CTRL_KEY = 341
        val MOVE_NODE_STRING: String = Text.translatableWithFallback(
            "trueadaptivemusic.move_node",
            "Click and drag to move + shift (copy) + ctrl (copy with children)."
        ).string
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