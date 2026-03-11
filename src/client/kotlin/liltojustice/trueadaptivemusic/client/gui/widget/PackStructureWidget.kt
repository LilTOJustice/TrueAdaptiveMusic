package liltojustice.trueadaptivemusic.client.gui.widget

import liltojustice.trueadaptivemusic.client.gui.extensions.getTriggerTooltipText
import liltojustice.trueadaptivemusic.client.gui.widget.utility.ClickableTextWidget
import liltojustice.trueadaptivemusic.client.gui.widget.utility.ContainerWidget
import liltojustice.trueadaptivemusic.client.music.pack.MusicPack
import liltojustice.trueadaptivemusic.client.trigger.predicate.ErrorPredicate
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import liltojustice.trueadaptivemusic.client.music.tree.MusicTree
import liltojustice.trueadaptivemusic.client.trigger.predicate.types.RootPredicate
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.tooltip.Tooltip
import net.minecraft.client.gui.widget.ClickableWidget
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
    private val onUnselectNode: () -> Unit,
    private val onUnselectPredicate: () -> Unit,
    x: Int = 0,
    y: Int = 0
): ContainerWidget(
    width, height, TITLE_TEXT.string, true, false, true, true, true, x, y)
{
    private var mouseButtonHeld = false
    private var shiftHeld = false
    private var ctrlHeld = false
    private var spaceHeld = false
    private var targetedNode: MusicTree.Node? = null
    private var targetedPredicate: MusicPredicate? = null
    private var collapsed = mutableMapOf<MusicTree.Node, Boolean>()

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
                if (node !in collapsed) {
                    collapsed[node] = false
                }

                node.parent?.let { parent ->
                    if (collapsed[parent] ?: false) {
                        collapsed[node] = true
                        if (node == targetedNode || node.predicates.any { it == targetedPredicate }) {
                            targetedNode = null
                            targetedPredicate = null
                            onUnselectNode()
                            onUnselectPredicate()
                        }
                        return@traverse
                    }
                }

                val isCollapsed = collapsed[node] ?: false
                val xOffset = (path.size - 1) * INDENT

                if (node.children.isNotEmpty()) {
                    val widget = addWidget(
                        ClickableTextWidget(
                            if (isCollapsed) "˃" else "˅",
                            showHighlight = false,
                            onClick = {
                                collapsed[node] = !isCollapsed
                                if (isCollapsed && shiftHeld) {
                                    expandRecursively(node)
                                }

                                initPredicateWidgets(targetedNode)
                            }
                        ),
                        row,
                        xOffset
                    )
                    widget.setTooltip(Tooltip.of(if (isCollapsed) EXPAND_TEXT else COLLAPSE_TEXT))
                }

                addWidget(NodeWidget(node, TargetNode(node, false)), row, xOffset + 7)
                row++
            },
            { node, path ->
                if ((node.parent != null &&
                            (node !== targetedNode || collapsed[node.parent] == true || collapsed[node] == true)) ||
                    (node.parent == null && collapsed[node] == true)) {
                    return@traverse
                }

                addWidget(
                    CreateNodeWidget(TargetNode(node, true)),
                    row++,
                    path.size * INDENT + 7
                )
            }
        )
    }

    override fun appendClickableNarrations(builder: NarrationMessageBuilder?) {
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (keyCode == SHIFT_KEY) {
            shiftHeld = true
        }

        if (keyCode == CTRL_KEY) {
            ctrlHeld = true
        }

        if (keyCode == SPACE_KEY) {
            spaceHeld = true
        }

        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun keyReleased(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (keyCode == SHIFT_KEY) {
            shiftHeld = false
        }

        if (keyCode == CTRL_KEY) {
            ctrlHeld = false
        }

        if (keyCode == SPACE_KEY) {
            spaceHeld = false
        }

        return super.keyReleased(keyCode, scanCode, modifiers)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        val result = super.mouseClicked(mouseX, mouseY, button)
        mouseButtonHeld = false
        forEachChild { child ->
            if (focusedWidget == child &&
                child is NodeWidget &&
                child.targetNode.node.parent != null &&
                child.isMouseOver(mouseX, mouseY)) {
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
            if (child !is AbstractNodeWidget
                || !child.isMouseOver(mouseX, mouseY)
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

            if (child.targetNode.isParent || spaceHeld) {
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
            val rowHeight = getRowHeight(textRenderer.fontHeight)

            if (spaceHeld && !child.targetNode.isParent) {
                context?.drawText(
                    textRenderer,
                    ARROW_TEXT,
                    child.x + INDENT - textRenderer.getWidth(ARROW_TEXT) - 2,
                    child.y + (rowHeight / 2).toInt(),
                    if (valid) Colors.WHITE else Colors.RED,
                    false
                )
            }
            else {
                context?.drawText(
                    textRenderer,
                    ARROW_TEXT,
                    child.x - textRenderer.getWidth(ARROW_TEXT) - 2,
                    child.y - (rowHeight / 2).toInt(),
                    if (valid) Colors.WHITE else Colors.RED,
                    false
                )
            }

            return@forEachChild
        }
    }

    private fun isMovingNode(): Boolean {
        return mouseButtonHeld && targetedNode != null
    }

    private fun expandRecursively(node: MusicTree.Node) {
        collapsed[node] = false
        node.children.forEach { expandRecursively(it) }
    }

    companion object {
        const val INDENT = 10
        const val SHIFT_KEY = 340
        const val CTRL_KEY = 341
        const val SPACE_KEY = 32
        val TITLE_TEXT: MutableText = Text.translatableWithFallback(
            "trueadaptivemusic.pack_structure", "Pack Structure")
        val MOVE_NODE_TEXT: MutableText = Text.translatableWithFallback(
            "trueadaptivemusic.move_node",
            "Click and drag to move\n+ shift (copy)\n+ ctrl (copy recursively)\n+ space (target children" +
                    " of node)"
        )
        val ARROW_TEXT: MutableText = Text.literal("→")
        val LINE_SPACE: MutableText = Text.literal("\n\n")
        val CREATE_CHILD_NODE_TEXT: MutableText = Text.translatableWithFallback(
            "trueadaptivemusic.create_child_node", "Create Child Node"
        )
        val CREATE_CHILD_NODE_ROOT_TEXT: MutableText = Text.translatableWithFallback(
            "trueadaptivemusic.create_child_node_root", "Create Child Node of Root"
        )
        val CREATE_NODE_TEXT: MutableText = Text.translatableWithFallback(
            "trueadaptivemusic.create_node", "Create New Node")
        val CREATE_PREDICATE_TEXT: MutableText = Text.translatableWithFallback(
            "trueadaptivemusic.create_predicate", "Create a Predicate")
        val COMBINE_PREDICATES_TEXT: MutableText = Text.translatableWithFallback(
            "trueadaptivemusic.combine_predicates", "Combine Predicates")
        val EMPTY_TEXT: MutableText = Text.translatableWithFallback("trueadaptivemusic.empty", "Empty")
        val CONFIGURE_NODE_TEXT: MutableText = Text.translatableWithFallback(
            "trueadaptivemusic.configure_node", "Configure this node")
        val EXPAND_TEXT: MutableText = Text.translatableWithFallback(
            "trueadaptivemusic.expand", "Click to Expand Children\n\nHold shift to expand recursively")
        val COLLAPSE_TEXT: MutableText = Text.translatableWithFallback(
            "trueadaptivemusic.collapse", "Click to Collapse Children")
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

        val combinePredicateWidget = if (!node.predicates.isEmpty() && node.predicates.any { it is RootPredicate })
            null
        else
            run {
                val widget = ClickableTextWidget(
                    if (node.predicates.isEmpty()) "${EMPTY_TEXT.string} +" else "+",
                    showHighlight = node.predicates.isEmpty(),
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
                    collapsed[node] = false
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

        override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
            val result = super.mouseClicked(mouseX, mouseY, button)
            val predicateClicked = predicateWidgets.any { it.mouseClicked(mouseX, mouseY, button) }
            val combineClicked = combinePredicateWidget?.mouseClicked(mouseX, mouseY, button) ?: false
            configureNodeWidget.mouseClicked(mouseX, mouseY, button)

            if (!predicateClicked && !combineClicked && result) {
                configureNodeWidget.onClick(mouseX, mouseY)
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

            return nextX + widget.width + 5
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