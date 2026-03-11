package liltojustice.trueadaptivemusic.client.gui.screen

import liltojustice.trueadaptivemusic.Logger
import liltojustice.trueadaptivemusic.client.TAMClient
import liltojustice.trueadaptivemusic.client.trigger.event.MusicEvent
import liltojustice.trueadaptivemusic.client.gui.widget.EventViewWidget
import liltojustice.trueadaptivemusic.client.gui.widget.NodeViewWidget
import liltojustice.trueadaptivemusic.client.gui.widget.PackStructureWidget
import liltojustice.trueadaptivemusic.client.gui.widget.PredicateViewWidget
import liltojustice.trueadaptivemusic.client.music.pack.MusicPack
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.tooltip.Tooltip
import net.minecraft.client.gui.widget.*
import net.minecraft.text.Text
import net.minecraft.util.Colors
import net.minecraft.util.Identifier
import net.minecraft.util.Util

@Environment(EnvType.CLIENT)
class EditPackScreen(private val parent: Screen, private val musicPack: MusicPack)
    : Screen(
    Text.translatableWithFallback(
        "trueadaptivemusic.create_edit_pack", "Create/Edit a music pack")) {
    private lateinit var packStructureWidget: PackStructureWidget
    private lateinit var nodeViewWidget: NodeViewWidget
    private lateinit var predicateViewWidget: PredicateViewWidget
    private lateinit var eventViewWidget: EventViewWidget
    private lateinit var saveButtonWidget: TextIconButtonWidget
    private lateinit var closeButtonWidget: ButtonWidget
    private lateinit var openAssetsFolderButtonWidget: ButtonWidget
    private lateinit var optionsButtonWidget: ButtonWidget

    private val predicateView: Boolean
        get() = predicateViewWidget.visible

    private val eventView: Boolean
        get() = eventViewWidget.visible

    private fun initPack() {
        TAMClient.playSoundNow(null)
        TAMClient.musicPack = MusicPack.fromFile(musicPack.initEdit(musicPack))
    }

    private fun exportAndClose() {
        TAMClient.musicPack = null
        val path = musicPack.save()
        TAMClient.musicPack = MusicPack.fromFile(path)
        if (parent is MainScreen) {
            parent.reload()
        }

        client?.setScreen(parent)
    }

    override fun init() {
        try {
            initPack()
        }
        catch (e: Exception) {
            TAMClient.errorToast(
                Text.translatableWithFallback(
                    "trueadaptivemusic.edit_load_error", "Failed to load pack to edit!"),
                e.message
            )
            Logger.logError("Failed to load pack to edit:\n$e")
            close()
        }

        saveButtonWidget = TextIconButtonWidget.Builder(SAVE_BUTTON_TEXT, {
            exportAndClose()
        }, false)
            .texture(CHECKMARK, 9, 8)
            .build()

        closeButtonWidget = ButtonWidget.Builder(CLOSE_BUTTON_TEXT) {
            close()
        }
            .build()

        openAssetsFolderButtonWidget = ButtonWidget.Builder(OPEN_ASSETS_TEXT) {
            Util.getOperatingSystem().open(musicPack.getEditPackAssetsPath().toUri())
        }
            .build()

        optionsButtonWidget = ButtonWidget.Builder(OPTIONS_BUTTON_TEXT) {
            client?.setScreen(PackOptionsScreen(this, musicPack))
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

        addDrawableChild(saveButtonWidget)
        addDrawableChild(closeButtonWidget)
        addDrawableChild(openAssetsFolderButtonWidget)
        addDrawableChild(packStructureWidget)
        addDrawableChild(nodeViewWidget)
        addDrawableChild(predicateViewWidget)
        addDrawableChild(eventViewWidget)
        addDrawableChild(optionsButtonWidget)

        saveButtonWidget.width = textRenderer.getWidth(saveButtonWidget.message) + 20
        closeButtonWidget.x = saveButtonWidget.x + saveButtonWidget.width + 5
        closeButtonWidget.width = textRenderer.getWidth(CLOSE_BUTTON_TEXT) + 10
        closeButtonWidget.setTooltip(
            Tooltip.of(
                Text.translatableWithFallback(
                    "trueadaptivemusic.change_save", "Changes will be saved")
            )
        )
        openAssetsFolderButtonWidget.width = textRenderer.getWidth(OPEN_ASSETS_TEXT) + 10
        openAssetsFolderButtonWidget.x = width - openAssetsFolderButtonWidget.width
        optionsButtonWidget.width = textRenderer.getWidth(OPTIONS_BUTTON_TEXT) + 10
        optionsButtonWidget.x = openAssetsFolderButtonWidget.x - optionsButtonWidget.width - 5

        switchToNodeView()
    }

    override fun close() {
        try {
            initPack()
        }
        catch (_: Exception) {}

        if (parent is MainScreen) {
            parent.reload()
        }

        client?.setScreen(parent)
    }

    override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        renderBackground(context, mouseX, mouseY, delta)
        super.render(context, mouseX, mouseY, delta)
        context?.drawCenteredTextWithShadow(
            this.textRenderer, this.title, this.width / 2, 22, Colors.WHITE)
    }

    private fun positionContainers() {
        val gridWidget = GridWidget()
        gridWidget.mainPositioner
            .marginLeft(LEFT_MARGIN / 2)
            .marginRight(RIGHT_MARGIN / 2)
        val adder: GridWidget.Adder = gridWidget.createAdder(2)

        packStructureWidget.height = getContainerHeight()
        nodeViewWidget.height = getContainerHeight()
        predicateViewWidget.height = getContainerHeight()
        eventViewWidget.height = getContainerHeight()

        if (predicateView) {
            adder.add(packStructureWidget)
            adder.add(predicateViewWidget)
        }
        else if (eventView) {
            val innerGridWidget = GridWidget()
            nodeViewWidget.height = getContainerHeight() / 2
            eventViewWidget.height = getContainerHeight() / 2
            innerGridWidget.add(nodeViewWidget, 0, 0)
            innerGridWidget.add(eventViewWidget, 1, 0)
            innerGridWidget.setRowSpacing(1)
            adder.add(packStructureWidget)
            adder.add(innerGridWidget)
            innerGridWidget.refreshPositions()
        }
        else {
            adder.add(packStructureWidget)
            adder.add(nodeViewWidget)
        }

        gridWidget.refreshPositions()
        SimplePositioningWidget.setPos(
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
        return (width * 0.5 - LEFT_MARGIN - RIGHT_MARGIN).toInt()
    }

    private fun getContainerHeight(): Int {
        return (height - TOP_MARGIN - BOTTOM_MARGIN)
    }

    companion object {
        private val CHECKMARK: Identifier = Identifier.ofVanilla("icon/checkmark")
        private const val TOP_MARGIN = 32
        private const val BOTTOM_MARGIN = TOP_MARGIN / 4
        private const val LEFT_MARGIN = TOP_MARGIN / 4
        private const val RIGHT_MARGIN = LEFT_MARGIN
        private val OPEN_ASSETS_TEXT = Text.translatableWithFallback(
            "trueadaptivemusic.show_assets", "Show Assets")
        private val SAVE_BUTTON_TEXT = Text.translatableWithFallback(
            "trueadaptivemusic.save_and_zip", "Export")
        private val CLOSE_BUTTON_TEXT = Text.translatableWithFallback("trueadaptivemusic.close", "Close")
        private val OPTIONS_BUTTON_TEXT = Text.translatableWithFallback(
            "trueadaptivemusic.options", "Options")
    }
}