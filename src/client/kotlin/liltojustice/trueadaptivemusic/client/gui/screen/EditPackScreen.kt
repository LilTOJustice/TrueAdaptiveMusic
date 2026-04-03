package liltojustice.trueadaptivemusic.client.gui.screen

import liltojustice.trueadaptivemusic.Logger
import liltojustice.trueadaptivemusic.client.TAMClient
import liltojustice.trueadaptivemusic.client.gui.widget.EventViewWidget
import liltojustice.trueadaptivemusic.client.gui.widget.NodeViewWidget
import liltojustice.trueadaptivemusic.client.gui.widget.PackStructureWidget
import liltojustice.trueadaptivemusic.client.gui.widget.PredicateViewWidget
import liltojustice.trueadaptivemusic.client.music.pack.MusicPack
import liltojustice.trueadaptivemusic.client.trigger.event.MusicEvent
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.ImageButton
import net.minecraft.client.gui.components.SpriteIconButton
import net.minecraft.client.gui.components.Tooltip
import net.minecraft.client.gui.layouts.GridLayout
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.util.CommonColors
import net.minecraft.util.Util

@Environment(EnvType.CLIENT)
class EditPackScreen(
    private val parent: Screen,
    private val musicPack: MusicPack
): Screen(
    Component.translatableWithFallback(
        "trueadaptivemusic.create_edit_pack", "Create/Edit a music pack")
) {
    private lateinit var packStructureWidget: PackStructureWidget
    private lateinit var nodeViewWidget: NodeViewWidget
    private lateinit var predicateViewWidget: PredicateViewWidget
    private lateinit var eventViewWidget: EventViewWidget
    private lateinit var saveButtonWidget: SpriteIconButton
    private lateinit var closeButtonWidget: Button
    private lateinit var openAssetsFolderButtonWidget: Button
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

    override fun mouseClicked(event: MouseButtonEvent, doubled: Boolean): Boolean {
        val optional = this.getChildAt(event.x, event.y)
        if (optional.isEmpty) {
            return false
        } else {
            val element = optional.get()
            if (element.mouseClicked(event, doubled)) {
                if (event.button() == 0) {
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
            Logger.logError("Failed to load pack to edit:\n$e")
            onClose()
        }

        saveButtonWidget = SpriteIconButton.builder(SAVE_BUTTON_TEXT, { exportAndClose() }, false)
            .sprite(CHECKMARK, 9, 8)
            .build()

        closeButtonWidget = Button.Builder(CLOSE_BUTTON_TEXT) { onClose() }.build()

        openAssetsFolderButtonWidget = Button.Builder(OPEN_ASSETS_TEXT) {
            Util.getPlatform().openUri(musicPack.getEditPackAssetsPath().toUri())
        }.build()

        optionsButtonWidget = Button.Builder(OPTIONS_BUTTON_TEXT) {
            minecraft.setScreen(PackOptionsScreen(this, musicPack))
        }.build()

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

        addWidget(saveButtonWidget)
        addWidget(closeButtonWidget)
        addWidget(openAssetsFolderButtonWidget)
        addWidget(packStructureWidget)
        addWidget(nodeViewWidget)
        addWidget(predicateViewWidget)
        addWidget(eventViewWidget)
        addWidget(optionsButtonWidget)

        saveButtonWidget.width = font.width(saveButtonWidget.message) + 20
        closeButtonWidget.x = saveButtonWidget.x + saveButtonWidget.width + 5
        closeButtonWidget.width = font.width(CLOSE_BUTTON_TEXT) + 10
        closeButtonWidget.setTooltip(
            Tooltip.create(
                Component.translatableWithFallback(
                    "trueadaptivemusic.change_save", "Changes will be saved")
            )
        )
        openAssetsFolderButtonWidget.width = font.width(OPEN_ASSETS_TEXT) + 10
        openAssetsFolderButtonWidget.x = width - openAssetsFolderButtonWidget.width
        optionsButtonWidget.width = font.width(OPTIONS_BUTTON_TEXT) + 10
        optionsButtonWidget.x = openAssetsFolderButtonWidget.x - optionsButtonWidget.width - 5

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

    override fun extractRenderState(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, a: Float) {
        super.extractRenderState(graphics, mouseX, mouseY, a)
        graphics.centeredText(font, title, width / 2, 22, CommonColors.WHITE)
    }

    private fun positionContainers() {
        val gridLayout = GridLayout()
        gridLayout.defaultCellSetting()
            .paddingLeft(LEFT_PADDING / 2)
            .paddingRight(RIGHT_PADDING / 2)
        val rowHelper: GridLayout.RowHelper = gridLayout.createRowHelper(2)

        packStructureWidget.height = getContainerHeight()
        nodeViewWidget.height = getContainerHeight()
        predicateViewWidget.height = getContainerHeight()
        eventViewWidget.height = getContainerHeight()

        if (predicateView) {
            rowHelper.addChild(packStructureWidget)
            rowHelper.addChild(predicateViewWidget)
        }
        else if (eventView) {
            val innerGridLayout = GridLayout()
            nodeViewWidget.height = getContainerHeight() / 2
            eventViewWidget.height = getContainerHeight() / 2
            innerGridLayout.addChild(nodeViewWidget, 0, 0)
            innerGridLayout.addChild(eventViewWidget, 1, 0)
            innerGridLayout.rowSpacing(1)
            rowHelper.addChild(packStructureWidget)
            rowHelper.addChild(innerGridLayout)
            innerGridLayout.arrangeElements()
        }
        else {
            rowHelper.addChild(packStructureWidget)
            rowHelper.addChild(nodeViewWidget)
        }

        gridLayout.arrangeElements()
        SimplePositioningWidget.setPos(
            gridLayout,
            LEFT_PADDING,
            TOP_MARGIN,
            RIGHT_PADDING,
            BOTTOM_MARGIN,
            0f,
            0f
        )
    }

    private fun switchToNodeView() {
        nodeViewWidget.visible = true
        eventViewWidget.visible = false
        predicateViewWidget.visible = false
        positionContainers()
    }

    private fun switchToEventView(event: MusicEvent?) {
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
        return (width * 0.5 - LEFT_PADDING - RIGHT_PADDING).toInt()
    }

    private fun getContainerHeight(): Int {
        return (height - TOP_MARGIN - BOTTOM_MARGIN)
    }

    companion object {
        private val CHECKMARK: Identifier = Identifier.withDefaultNamespace("icon/checkmark")
        private const val TOP_MARGIN = 32
        private const val BOTTOM_MARGIN = TOP_MARGIN / 4
        private const val LEFT_PADDING = TOP_MARGIN / 4
        private const val RIGHT_PADDING = LEFT_PADDING
        private val OPEN_ASSETS_TEXT = Component.translatableWithFallback(
            "trueadaptivemusic.show_assets", "Show Assets")
        private val SAVE_BUTTON_TEXT = Component.translatableWithFallback(
            "trueadaptivemusic.save_and_zip", "Export")
        private val CLOSE_BUTTON_TEXT = Component.translatableWithFallback(
            "trueadaptivemusic.close", "Close")
        private val OPTIONS_BUTTON_TEXT = Component.translatableWithFallback(
            "trueadaptivemusic.options", "Options")
    }
}