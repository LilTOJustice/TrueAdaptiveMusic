package liltojustice.trueadaptivemusic.client.gui.screen

import liltojustice.trueadaptivemusic.Logger
import liltojustice.trueadaptivemusic.client.TAMClient
import liltojustice.trueadaptivemusic.client.gui.widget.EventViewWidget
import liltojustice.trueadaptivemusic.client.gui.widget.NodeViewWidget
import liltojustice.trueadaptivemusic.client.gui.widget.PackStructureWidget
import liltojustice.trueadaptivemusic.client.gui.widget.PredicateViewWidget
import liltojustice.trueadaptivemusic.client.music.pack.MusicPack
import liltojustice.trueadaptivemusic.client.trigger.event.MusicEvent
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.components.Tooltip
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.layouts.GridLayout
import net.minecraft.client.gui.layouts.FrameLayout
import net.minecraft.client.gui.components.SpriteIconButton
import net.minecraft.network.chat.Component
import net.minecraft.util.CommonColors
import net.minecraft.resources.Identifier
import net.minecraft.util.Util

class EditPackScreen(private val parent: Screen, private val musicPack: MusicPack)
    : Screen(
    Component.translatableWithFallback(
        "trueadaptivemusic.create_edit_pack", "Create/Edit a music pack")) {
    private lateinit var packStructureWidget: PackStructureWidget
    private lateinit var nodeViewWidget: NodeViewWidget
    private lateinit var predicateViewWidget: PredicateViewWidget
    private lateinit var eventViewWidget: EventViewWidget
    private lateinit var saveButtonWidget: SpriteIconButton
    private lateinit var closeButtonWidget: Button
    private lateinit var openPackDirectory: Button
    private lateinit var optionsButtonWidget: Button

    private val predicateView: Boolean
        get() = predicateViewWidget.visible

    private val eventView: Boolean
        get() = eventViewWidget.visible

    private fun initPack() {
        TAMClient.playSoundNow(null)
        TAMClient.musicPack = MusicPack.fromFile(musicPack.initEdit(musicPack))
    }

    private fun exportAndClose() {
        minecraft.setScreen(ExportPackScreen(musicPack, parent))
    }

    override fun mouseClicked(click: MouseButtonEvent, doubled: Boolean): Boolean {
        val optional = this.getChildAt(click.x(), click.y())
        if (optional.isEmpty) {
            return false
        } else {
            val element = optional.get()
            if (element.mouseClicked(click, doubled) && element.shouldTakeFocusAfterInteraction()) {
                if (click.button() == 0) {
                    this.isDragging = true
                }
            }

            return true
        }
    }

    override fun init() {
        try {
            initPack()
        }
        catch (e: Exception) {
            TAMClient.errorToast(
                Component.translatableWithFallback(
                    "trueadaptivemusic.edit_load_error", "Failed to load pack to edit!"),
                e.message
            )
            Logger.logError("Failed to load pack to edit:\n${e.stackTraceToString()}")
            onClose()
        }

        saveButtonWidget = SpriteIconButton.Builder(SAVE_BUTTON_TEXT, {
            exportAndClose()
        }, false)
            .sprite(CHECKMARK, 9, 8)
            .build()

        closeButtonWidget = Button.Builder(CLOSE_BUTTON_TEXT) {
            onClose()
        }
            .build()

        openPackDirectory = Button.Builder(OPEN_PACK_TEXT) {
            Util.getPlatform().openUri(musicPack.packPath.toUri())
        }.build()

        optionsButtonWidget = Button.Builder(OPTIONS_BUTTON_TEXT) {
            minecraft.setScreen(PackOptionsScreen(this, musicPack))
        }
            .build()

        val containerWidth = getContainerWidth()
        val containerHeight = getContainerHeight()

        nodeViewWidget = NodeViewWidget(
            containerWidth,
            containerHeight,
            musicPack,
            { newTarget -> packStructureWidget.initPredicateWidgets(newTarget) },
            { event -> switchToEventView(event) },
            { eventView }
        )

        predicateViewWidget = PredicateViewWidget(
            containerWidth,
            containerHeight,
            musicPack,
            { targetNode, targetPredicate, exit ->
                packStructureWidget.setNode(targetNode, targetPredicate)
                packStructureWidget.initPredicateWidgets()

                if (exit) {
                    nodeViewWidget.reset()
                    switchToNodeView()
                }
            }
        )

        packStructureWidget = PackStructureWidget(
            containerWidth,
            containerHeight,
            musicPack,
            { node ->
                nodeViewWidget.setEditExistingNode(node)
                switchToNodeView()
            },
            { node, predicate ->
                predicateViewWidget.setEditExistingPredicate(node, predicate)
                switchToPredicateView()
            },
            { node ->
                nodeViewWidget.setCreateNewNode(node)
                switchToNodeView()
            },
            { parent ->
                predicateViewWidget.setCreateNewPredicate(parent)
                switchToPredicateView()
            },
            { nodeViewWidget.reset() },
            { switchToNodeView() }
        )

        eventViewWidget = EventViewWidget(
            containerWidth,
            containerHeight,
            musicPack,
            { newEvent, exit ->
                nodeViewWidget.onEventModeSave(newEvent, exit)
                if (exit) {
                    switchToNodeView()
                }
            }
        )

        addRenderableWidget(saveButtonWidget)
        addRenderableWidget(closeButtonWidget)
        addRenderableWidget(openPackDirectory)
        addRenderableWidget(packStructureWidget)
        addRenderableWidget(nodeViewWidget)
        addRenderableWidget(predicateViewWidget)
        addRenderableWidget(eventViewWidget)
        addRenderableWidget(optionsButtonWidget)

        saveButtonWidget.width = font.width(saveButtonWidget.message) + 20
        closeButtonWidget.x = saveButtonWidget.x + saveButtonWidget.width + 5
        closeButtonWidget.width = font.width(CLOSE_BUTTON_TEXT) + 10
        closeButtonWidget.setTooltip(
            Tooltip.create(
                Component.translatableWithFallback(
                    "trueadaptivemusic.change_save", "Changes will be saved")
            )
        )
        openPackDirectory.width = font.width(OPEN_PACK_TEXT) + 10
        openPackDirectory.x = width - openPackDirectory.width
        optionsButtonWidget.width = font.width(OPTIONS_BUTTON_TEXT) + 10
        optionsButtonWidget.x = openPackDirectory.x - optionsButtonWidget.width - 5

        switchToNodeView()
    }

    override fun onClose() {
        try {
            initPack()
        }
        catch (_: Exception) {}

        if (parent is MainScreen) {
            parent.reload()
        }

        minecraft.setScreen(parent)
    }

    override fun render(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(context, mouseX, mouseY, delta)
        context.drawCenteredString(
            this.font, this.title, this.width / 2, 22, CommonColors.WHITE)
    }

    private fun positionContainers() {
        val gridWidget = GridLayout()
        gridWidget.defaultCellSetting()
            .paddingLeft(LEFT_MARGIN / 2)
            .paddingRight(RIGHT_MARGIN / 2)
        val adder: GridLayout.RowHelper = gridWidget.createRowHelper(2)

        packStructureWidget.height = getContainerHeight()
        nodeViewWidget.height = getContainerHeight()
        predicateViewWidget.height = getContainerHeight()
        eventViewWidget.height = getContainerHeight()

        if (predicateView) {
            adder.addChild(packStructureWidget)
            adder.addChild(predicateViewWidget)
        }
        else if (eventView) {
            val innerGridWidget = GridLayout()
            nodeViewWidget.height = getContainerHeight() / 2
            eventViewWidget.height = getContainerHeight() / 2
            innerGridWidget.addChild(nodeViewWidget, 0, 0)
            innerGridWidget.addChild(eventViewWidget, 1, 0)
            innerGridWidget.rowSpacing(1)
            adder.addChild(packStructureWidget)
            adder.addChild(innerGridWidget)
            innerGridWidget.arrangeElements()
        }
        else {
            adder.addChild(packStructureWidget)
            adder.addChild(nodeViewWidget)
        }

        gridWidget.arrangeElements()
        FrameLayout.alignInRectangle(
            gridWidget,
            LEFT_MARGIN,
            TOP_MARGIN,
            RIGHT_MARGIN,
            BOTTOM_MARGIN,
            0f,
            0f)
    }

    private fun switchToNodeView() {
        nodeViewWidget.visible = true
        eventViewWidget.visible = false
        predicateViewWidget.visible = false
        positionContainers()
    }

    private fun switchToEventView(event: MusicEvent<*>?) {
        eventViewWidget.visible = true
        eventViewWidget.setEvent(event)
        positionContainers()
    }

    private fun switchToPredicateView() {
        predicateViewWidget.visible = true
        nodeViewWidget.visible = false
        eventViewWidget.visible = false
        positionContainers()
    }

    private fun getContainerWidth(): Int {
        return (width * 0.5 - LEFT_MARGIN - RIGHT_MARGIN).toInt()
    }

    private fun getContainerHeight(): Int {
        return (height - TOP_MARGIN - BOTTOM_MARGIN)
    }

    companion object {
        private val CHECKMARK: Identifier = Identifier.withDefaultNamespace("icon/checkmark")
        private const val TOP_MARGIN = 32
        private const val BOTTOM_MARGIN = TOP_MARGIN / 4
        private const val LEFT_MARGIN = TOP_MARGIN / 4
        private const val RIGHT_MARGIN = LEFT_MARGIN
        private val OPEN_PACK_TEXT = Component.translatableWithFallback(
            "trueadaptivemusic.open_pack", "Open Pack")
        private val SAVE_BUTTON_TEXT = Component.translatableWithFallback(
            "trueadaptivemusic.save_and_zip", "Export")
        private val CLOSE_BUTTON_TEXT = Component.translatableWithFallback("trueadaptivemusic.close", "Close")
        private val OPTIONS_BUTTON_TEXT = Component.translatableWithFallback(
            "trueadaptivemusic.options", "Options")
    }
}