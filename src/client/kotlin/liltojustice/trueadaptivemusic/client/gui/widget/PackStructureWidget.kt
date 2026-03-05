package liltojustice.trueadaptivemusic.client.gui.widget

import liltojustice.trueadaptivemusic.client.gui.extensions.getTriggerTooltipText
import liltojustice.trueadaptivemusic.client.gui.widget.utility.ClickableTextWidget
import liltojustice.trueadaptivemusic.client.gui.widget.utility.ContainerWidget
import liltojustice.trueadaptivemusic.client.music.pack.MusicPack
import liltojustice.trueadaptivemusic.client.trigger.predicate.ErrorPredicate
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import liltojustice.trueadaptivemusic.client.music.tree.MusicTree
import liltojustice.trueadaptivemusic.client.trigger.predicate.types.RootPredicate
import net.minecraft.client.gui.Click
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.tooltip.Tooltip
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.client.input.KeyInput
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Colors
import kotlin.math.max

class PackStructureWidget(
    width: Int,
    height: Int,
    private val musicPack: MusicPack,
    private val onSelectEditExistingNode: (node: MusicTree.Node) -> Unit,
    private val onSelectEditExistingPredicate: (node: MusicTree.Node, predicate: MusicPredicate) -> Unit,
    private val onSelectCreateNewNode: (node: MusicTree.Node) -> Unit,
    private val onSelectCreateNewPredicate: (node: MusicTree.Node) -> Unit,
    x: Int = 0,
    y: Int = 0
): ContainerWidget(
    width, height, TITLE_TEXT.string, true, false, true, true, x, y)
{
    private var mouseButtonHeld = false
    private var shiftHeld = false
    private var ctrlHeld = false
    private var targetedNode: MusicTree.Node? = null
    private var targetedPredicate: MusicPredicate? = null

    init {
        initPredicateWidgets()
    }

    fun setNode(node: MusicTree.Node?, predicate: MusicPredicate?) {
        targetedNode = node
        targetedPredicate = predicate
    }

    fun initPredicateWidgets(newTarget: MusicTree.Node? = null) {
        targetedNode = newTarget
        clearWidgets()
        var row = 0
        musicPack.rules.traverse(
            { node, path ->
                addWidget(
                    NodeWidget(node, TargetNode(node, false)),
                    row++,
                    (path.size - 1) * INDENT
                )
            },
            { node, path ->
                if (node.parent != null && node !== targetedNode) {
                    return@traverse
                }

                addWidget(
                    CreateNodeWidget(TargetNode(node, true)),
                    row++,
                    path.size * INDENT
                )
            }
        )
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
            if (child is NodeWidget &&
                child.targetNode.node.parent != null &&
                child.isMouseOver(click.x, click.y)) {
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
            if (child !is AbstractNodeWidget
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
            initPredicateWidgets(toAdopt)
        }

        mouseButtonHeld = false

        return result
    }

    override fun renderWidget(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        super.renderWidget(context, mouseX, mouseY, delta)

        if (!isMovingNode()) {
            return
        }

        forEachChild { child ->
            if (child !is AbstractNodeWidget
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
        val TITLE_TEXT: MutableText = Text.translatableWithFallback(
            "trueadaptivemusic.pack_structure", "Pack Structure")
        val MOVE_NODE_TEXT: MutableText = Text.translatableWithFallback(
            "trueadaptivemusic.move_node",
            "Click and drag to move\n+ shift (copy)\n+ ctrl (copy with children)"
        )
        val ARROW_TEXT: MutableText = Text.literal("->")
        val LINE_SPACE: MutableText = Text.literal("\n\n")
        val CREATE_CHILD_NODE_TEXT: MutableText = Text.translatableWithFallback(
            "trueadaptivemusic.create_child_node", "Create Child Node"
        )
        val CREATE_CHILD_NODE_ROOT_TEXT: MutableText = Text.translatableWithFallback(
            "trueadaptivemusic.create_child_node_root", "Create Child Node of Root"
        )
        val CREATE_NODE_TEXT: MutableText = Text.translatableWithFallback(
            "trueadaptivemusic.create_node", "Create new node")
        val CREATE_PREDICATE_TEXT: MutableText = Text.translatableWithFallback(
            "trueadaptivemusic.create_predicate", "Create a Predicate")
        val COMBINE_PREDICATES_TEXT: MutableText = Text.translatableWithFallback(
            "trueadaptivemusic.combine_predicates", "Combine Predicates")
        val EMPTY_TEXT: MutableText = Text.translatableWithFallback("trueadaptivemusic.empty", "Empty")
        val CONFIGURE_NODE_TEXT: MutableText = Text.translatableWithFallback(
            "trueadaptivemusic.configure_node", "Configure this node")
    }

    private inner class NodeWidget(node: MusicTree.Node, override val targetNode: TargetNode):
        AbstractNodeWidget,
        ClickableTextWidget(
            "", showHighlight = false, isSelected = { targetedNode === node && targetedPredicate == null })
    {
        init {
            active = true
        }

        val predicateWidgets = run {
            node.predicates.map { predicate ->
                val widget = ClickableTextWidget(
                    MusicPredicate.getDisplayName(predicate.getTypeName()).string,
                    onClick = if (predicate is RootPredicate) ({
                        targetedPredicate = null
                        targetedNode = node
                        onSelectEditExistingNode(node)
                    })
                    else ({
                        targetedPredicate = predicate
                        onSelectEditExistingPredicate(node, predicate)
                    }),
                    isSelected = { predicate === targetedPredicate }
                )

                val tooltipText = predicate.getTriggerTooltipText()

                widget.setTooltip(Tooltip.of(tooltipText))
                widget.color = if (predicate is ErrorPredicate)
                    Colors.RED
                else
                    Colors.WHITE

                widget
            }
        }

        val orWidgets = buildList(max(0, predicateWidgets.size - 1)) {
            repeat(max(0, predicateWidgets.size - 1)) { add(ClickableTextWidget("||")) }
        }

        val combinePredicateWidget = if (!node.predicates.isEmpty() &&
            (node.predicates.any { it is RootPredicate } ||
                    (node !== targetedNode && node.predicates.none { it === targetedPredicate })))
            null
        else
            run {
                val widget = ClickableTextWidget(
                    if (node.predicates.isEmpty()) "${EMPTY_TEXT.string} +" else "+",
                    showHighlight = false,
                    onClick = {
                        onSelectCreateNewPredicate(node)
                        initPredicateWidgets()
                    }
                )

                if (node.predicates.isEmpty()) {
                    widget.enableItalic()
                }

                widget.setTooltip(
                    Tooltip.of(
                        if (node.predicates.isEmpty())
                            CREATE_PREDICATE_TEXT
                        else
                            COMBINE_PREDICATES_TEXT
                    )
                )

                widget
            }

        val configureNodeWidget = run {
            val widget = ClickableTextWidget(
                if (targetedNode == node && node.parent != null) "⠿⠿" else "⚙",
                showHighlight = false,
                onClick = {
                    targetedPredicate = null
                    targetedNode = node
                    onSelectEditExistingNode(node)
                }
            )
            val tooltipText = if (targetedNode !== node)
                CONFIGURE_NODE_TEXT.copyContentOnly().append(LINE_SPACE).append(MOVE_NODE_TEXT)
            else
                MOVE_NODE_TEXT
            widget.setTooltip(Tooltip.of(tooltipText))

            widget
        }

        override fun mouseClicked(click: Click, doubled: Boolean): Boolean {
            val result = super.mouseClicked(click, doubled)
            val predicateClicked = predicateWidgets.any { it.mouseClicked(click, doubled) }
            val combineClicked = combinePredicateWidget?.mouseClicked(click, doubled) ?: false
            configureNodeWidget.mouseClicked(click, doubled)

            if (!predicateClicked && !combineClicked && result) {
                configureNodeWidget.onClick(click, doubled)
            }

            return result
        }

        override fun renderWidget(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
            super.renderWidget(context, mouseX, mouseY, delta)
            var nextX = 0

            nextX = renderWidget(configureNodeWidget, nextX) {
                configureNodeWidget.render(context, mouseX, mouseY, delta)
            }
            predicateWidgets.forEachIndexed { index, widget ->
                nextX = renderWidget(widget, nextX) { widget.render(context, mouseX, mouseY, delta) }

                if (index < orWidgets.size) {
                    val orWidget = orWidgets[index]
                    nextX = renderWidget(orWidget, nextX) { orWidget.render(context, mouseX, mouseY, delta) }
                }
            }

            combinePredicateWidget?.let {
                renderWidget(it, nextX) {
                    it.render(context, mouseX, mouseY, delta)
                }
            }
        }

        override fun appendClickableNarrations(builder: NarrationMessageBuilder?) {
        }

        private fun renderWidget(widget: ClickableWidget, nextX: Int, render: () -> Unit): Int {
            widget.x = x + nextX
            widget.y = y
            width = max(width, nextX + widget.width)
            height = max(height, widget.height)
            render()

            return nextX + widget.width + 4
        }
    }

    private inner class CreateNodeWidget(override val targetNode: TargetNode):
        AbstractNodeWidget,
        ClickableTextWidget(
            "+ ${
                (if (targetNode.node.parent == null) CREATE_CHILD_NODE_ROOT_TEXT else CREATE_CHILD_NODE_TEXT).string}",
            onClick = { onSelectCreateNewNode(targetNode.node) }
        ) {
        init {
            setTooltip(Tooltip.of(CREATE_NODE_TEXT))
        }
    }

    private interface AbstractNodeWidget {
        val targetNode: TargetNode
        fun isValidDestination(selectedNode: MusicTree.Node): Boolean {
            return (targetNode.node.parent != null || targetNode.isParent)
                    && targetNode.node.isValidNewChild(selectedNode)
        }
    }

    private data class TargetNode(val node: MusicTree.Node, val isParent: Boolean)
}