package liltojustice.trueadaptivemusic.common.client.gui.widget

import liltojustice.trueadaptivemusic.common.client.gui.extensions.getTriggerTooltipText
import liltojustice.trueadaptivemusic.common.client.gui.widget.utility.ClickableTextWidget
import liltojustice.trueadaptivemusic.common.client.gui.widget.utility.ContainerWidget
import liltojustice.trueadaptivemusic.common.client.music.pack.MusicPack
import liltojustice.trueadaptivemusic.common.client.trigger.predicate.MusicPredicate
import liltojustice.trueadaptivemusic.common.client.music.tree.MusicTree
import liltojustice.trueadaptivemusic.common.client.trigger.predicate.ErrorPredicate
import liltojustice.trueadaptivemusic.common.client.trigger.predicate.types.RootPredicate
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.components.Tooltip
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.util.CommonColors
import kotlin.math.max

class PackStructureWidget(
    width: Int,
    height: Int,
    private val musicPack: MusicPack,
    private val onSelectEditExistingNode: (node: MusicTree.Node) -> Unit,
    private val onSelectEditExistingPredicate: (node: MusicTree.Node, predicate: MusicPredicate<*>) -> Unit,
    private val onSelectCreateNewNode: (node: MusicTree.Node) -> Unit,
    private val onSelectCreateNewPredicate: (node: MusicTree.Node) -> Unit,
    private val onUnselectNode: () -> Unit,
    private val onUnselectPredicate: () -> Unit,
    x: Int = 0,
    y: Int = 0
): ContainerWidget(
    width,
    height,
    TITLE_TEXT.string,
    true,
    false,
    true,
    true,
    true,
    x,
    y
) {
    private var mouseButtonHeld = false
    private var shiftHeld = false
    private var ctrlHeld = false
    private var spaceHeld = false
    private var targetedNode: MusicTree.Node? = null
    private var targetedPredicate: MusicPredicate<*>? = null
    private var collapsed = mutableMapOf<MusicTree.Node, Boolean>()

    init {
        initPredicateWidgets()
    }

    fun setNode(node: MusicTree.Node?, predicate: MusicPredicate<*>?) {
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
                    widget.setTooltip(Tooltip.create(if (isCollapsed) EXPAND_TEXT else COLLAPSE_TEXT))
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

    override fun updateWidgetNarration(output: NarrationElementOutput) {
    }

    override fun keyPressed(event: KeyEvent): Boolean {
        if (event.key == SHIFT_KEY) {
            shiftHeld = true
        }

        if (event.key == CTRL_KEY) {
            ctrlHeld = true
        }

        if (event.key == SPACE_KEY) {
            spaceHeld = true
        }

        return super.keyPressed(event)
    }

    override fun keyReleased(event: KeyEvent): Boolean {
        if (event.key == SHIFT_KEY) {
            shiftHeld = false
        }

        if (event.key == CTRL_KEY) {
            ctrlHeld = false
        }

        if (event.key == SPACE_KEY) {
            spaceHeld = false
        }

        return super.keyReleased(event)
    }

    override fun mouseClicked(event: MouseButtonEvent, doubleClick: Boolean): Boolean {
        val result = super.mouseClicked(event, doubleClick)
        mouseButtonHeld = false
        visitWidgets { child ->
            if (focusedWidget == child &&
                child is NodeWidget &&
                child.targetNode.node.parent != null &&
                child.isMouseOver(event.x, event.y)) {
                mouseButtonHeld = true

                return@visitWidgets
            }
        }

        return result
    }

    override fun mouseReleased(event: MouseButtonEvent): Boolean {
        val result = super.mouseReleased(event)

        if (!isMovingNode()) {
            return result
        }

        visitWidgets { child ->
            if (child !is AbstractNodeWidget
                || !child.isMouseOver(event.x, event.y)
                || (targetedNode === child.targetNode.node && !shiftHeld)
                || targetedNode?.let { child.isValidDestination(it) || shiftHeld } != true) {
                return@visitWidgets
            }

            val targetNode = child.targetNode.node
            val toAdopt = targetedNode?.let {
                if (shiftHeld) {
                    it.copy(ctrlHeld)
                }
                else {
                    it
                }
            } ?: return@visitWidgets

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

    override fun extractWidgetRenderState(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, a: Float) {
        super.extractWidgetRenderState(graphics, mouseX, mouseY, a)

        if (!isMovingNode()) {
            return
        }

        visitWidgets { child ->
            if (child !is AbstractNodeWidget
                || !child.isMouseOver(mouseX.toDouble(), mouseY.toDouble())
                || (child.targetNode.node === targetedNode && !shiftHeld)) {
                return@visitWidgets
            }

            val valid = targetedNode?.let { child.isValidDestination(it) || shiftHeld } == true
            val rowHeight = getRowHeight(font.lineHeight)

            if (spaceHeld && !child.targetNode.isParent) {
                graphics.text(
                    font,
                    ARROW_TEXT,
                    child.x + INDENT - font.width(ARROW_TEXT) - 2,
                    child.y + (rowHeight / 2).toInt(),
                    if (valid) CommonColors.WHITE else CommonColors.RED,
                    false
                )
            }
            else {
                graphics.text(
                    font,
                    ARROW_TEXT,
                    child.x - font.width(ARROW_TEXT) - 2,
                    child.y - (rowHeight / 2).toInt(),
                    if (valid) CommonColors.WHITE else CommonColors.RED,
                    false
                )
            }

            return@visitWidgets
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
        val TITLE_TEXT: MutableComponent = Component.translatableWithFallback(
            "trueadaptivemusic.pack_structure", "Pack Structure")
        val MOVE_NODE_TEXT: MutableComponent = Component.translatableWithFallback(
            "trueadaptivemusic.move_node",
            "Click and drag to move\n+ shift (copy)\n+ ctrl (copy recursively)\n+ space (target children" +
                    " of node)"
        )
        val ARROW_TEXT: MutableComponent = Component.literal("→")
        val LINE_SPACE: MutableComponent = Component.literal("\n\n")
        val CREATE_CHILD_NODE_TEXT: MutableComponent = Component.translatableWithFallback(
            "trueadaptivemusic.create_child_node", "Create Child Node"
        )
        val CREATE_CHILD_NODE_ROOT_TEXT: MutableComponent = Component.translatableWithFallback(
            "trueadaptivemusic.create_child_node_root", "Create Child Node of Root"
        )
        val CREATE_NODE_TEXT: MutableComponent = Component.translatableWithFallback(
            "trueadaptivemusic.create_node", "Create New Node")
        val CREATE_PREDICATE_TEXT: MutableComponent = Component.translatableWithFallback(
            "trueadaptivemusic.create_predicate", "Create a Predicate")
        val COMBINE_PREDICATES_TEXT: MutableComponent = Component.translatableWithFallback(
            "trueadaptivemusic.combine_predicates", "Combine Predicates")
        val EMPTY_TEXT: MutableComponent = Component.translatableWithFallback("trueadaptivemusic.empty", "Empty")
        val CONFIGURE_NODE_TEXT: MutableComponent = Component.translatableWithFallback(
            "trueadaptivemusic.configure_node", "Configure this node")
        val EXPAND_TEXT: MutableComponent = Component.translatableWithFallback(
            "trueadaptivemusic.expand", "Click to Expand Children\n\nHold shift to expand recursively")
        val COLLAPSE_TEXT: MutableComponent = Component.translatableWithFallback(
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
                    MusicPredicate.getDisplayName(predicate.type.typeName).string,
                    onClick = if (predicate.type is RootPredicate) ({
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

                widget.setTooltip(Tooltip.create(tooltipText))
                widget.color = if (predicate.type is ErrorPredicate)
                    CommonColors.RED
                else
                    CommonColors.WHITE

                widget
            }
        }

        val orWidgets = buildList(max(0, predicateWidgets.size - 1)) {
            repeat(max(0, predicateWidgets.size - 1)) { add(ClickableTextWidget("||")) }
        }

        val combinePredicateWidget = if (!node.predicates.isEmpty() && node.predicates.any { it.type is RootPredicate })
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
                    widget.withItalic()
                }

                widget.setTooltip(
                    Tooltip.create(
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
                CONFIGURE_NODE_TEXT.copy().append(LINE_SPACE).append(MOVE_NODE_TEXT)
            else
                MOVE_NODE_TEXT
            widget.setTooltip(Tooltip.create(tooltipText))

            widget
        }

        override fun mouseClicked(event: MouseButtonEvent, doubled: Boolean): Boolean {
            val result = super.mouseClicked(event, doubled)
            val predicateClicked = predicateWidgets.any { it.mouseClicked(event, doubled) }
            val combineClicked = combinePredicateWidget?.mouseClicked(event, doubled) ?: false
            configureNodeWidget.mouseClicked(event, doubled)

            if (!predicateClicked && !combineClicked && result) {
                configureNodeWidget.onClick(event, doubled)
            }

            return result
        }

        override fun extractWidgetRenderState(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, a: Float) {
            super.extractWidgetRenderState(graphics, mouseX, mouseY, a)
            var nextX = 0

            nextX = renderWidget(configureNodeWidget, nextX) {
                configureNodeWidget.extractRenderState(graphics, mouseX, mouseY, a)
            }
            predicateWidgets.forEachIndexed { index, widget ->
                nextX = renderWidget(widget, nextX) { widget.extractRenderState(graphics, mouseX, mouseY, a) }

                if (index < orWidgets.size) {
                    val orWidget = orWidgets[index]
                    nextX = renderWidget(
                        orWidget, nextX) { orWidget.extractRenderState(graphics, mouseX, mouseY, a) }
                }
            }

            combinePredicateWidget?.let {
                renderWidget(it, nextX) {
                    it.extractRenderState(graphics, mouseX, mouseY, a)
                }
            }
        }

        override fun updateWidgetNarration(output: NarrationElementOutput) {
        }

        private fun renderWidget(widget: AbstractWidget, nextX: Int, render: () -> Unit): Int {
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
            setTooltip(Tooltip.create(CREATE_NODE_TEXT))
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