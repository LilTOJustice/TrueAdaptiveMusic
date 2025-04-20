package liltojustice.trueadaptivemusic.client.gui.screen

import liltojustice.trueadaptivemusic.client.TAMClient
import liltojustice.trueadaptivemusic.client.trigger.event.MusicEvent
import liltojustice.trueadaptivemusic.client.gui.widget.EventViewWidget
import liltojustice.trueadaptivemusic.client.gui.widget.PackStructureWidget
import liltojustice.trueadaptivemusic.client.gui.widget.PredicateViewWidget
import liltojustice.trueadaptivemusic.client.music.MusicPack
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
    : Screen(Text.literal("Create/Edit a music pack")) {
    private lateinit var predicateViewWidget: PredicateViewWidget
    private lateinit var packStructureWidget: PackStructureWidget
    private lateinit var eventViewWidget: EventViewWidget
    private lateinit var saveButtonWidget: IconButtonWidget
    private lateinit var closeButtonWidget: ButtonWidget
    private lateinit var openAssetsFolderButtonWidget: ButtonWidget
    private var selectedEvent: MusicEvent? = null

    private val eventView: Boolean
        get() = eventViewWidget.visible

    private fun initPack() {
        TAMClient.playSoundNow(null)
        val newPath = musicPack.initEdit(musicPack)
        TAMClient.musicPack = MusicPack.fromFile(newPath)
    }

    override fun init() {
        initPack()

        saveButtonWidget = IconButtonWidget.Builder(SAVE_BUTTON_TEXT, CHECKMARK) {
            TAMClient.musicPack = null
            val path = musicPack.save()
            TAMClient.musicPack = MusicPack.fromFile(path)
            this.close()
        }
            .iconSize(9, 8)
            .textureSize(9, 8)
            .xyOffset(32, 6)
            .build()

        closeButtonWidget = ButtonWidget.Builder(CLOSE_BUTTON_TEXT) {
            close()
        }
            .build()

        openAssetsFolderButtonWidget = ButtonWidget.Builder(OPEN_ASSETS_TEXT) {
            Util.getOperatingSystem().open(musicPack.getEditPackAssetsPath().toUri())
        }
            .build()

        predicateViewWidget = PredicateViewWidget(
            getContainerWidth(),
            getContainerHeight(),
            musicPack,
            {
                initPack()
                packStructureWidget.initPredicateWidgets()
            },
            { event -> switchToEventView(event) },
            { eventView })
        packStructureWidget = PackStructureWidget(
            getContainerWidth(),
            getContainerHeight(),
            musicPack,
            { node ->
                predicateViewWidget.setEditExistingNode(node)
                switchToPredicateView()
            },
            { parent ->
                predicateViewWidget.setCreateNewNode(parent)
                switchToPredicateView()
            })
        eventViewWidget = EventViewWidget(
            getContainerWidth(),
            getContainerHeight(),
            musicPack,
            { newEvent ->
                predicateViewWidget.onEventModeExit(newEvent)
                switchToPredicateView() }
        )

        addDrawableChild(saveButtonWidget)
        addDrawableChild(closeButtonWidget)
        addDrawableChild(openAssetsFolderButtonWidget)
        addDrawableChild(predicateViewWidget)
        addDrawableChild(packStructureWidget)
        addDrawableChild(eventViewWidget)

        saveButtonWidget.width = 90
        closeButtonWidget.x = saveButtonWidget.x + saveButtonWidget.width + 5
        closeButtonWidget.width = textRenderer.getWidth(CLOSE_BUTTON_TEXT) + 10
        closeButtonWidget.tooltip = Tooltip.of(Text.literal("Changes will be saved"))
        openAssetsFolderButtonWidget.width = textRenderer.getWidth(OPEN_ASSETS_TEXT) + 10
        openAssetsFolderButtonWidget.x = width - openAssetsFolderButtonWidget.width

        val containerWidth = getContainerWidth()
        val containerHeight = getContainerHeight()
        packStructureWidget.width = containerWidth
        packStructureWidget.height = containerHeight
        predicateViewWidget.width = containerWidth
        predicateViewWidget.height = containerHeight
        eventViewWidget.width = containerWidth
        eventViewWidget.height = containerHeight

        if (selectedEvent == null) {
            switchToPredicateView()
        }
        else {
            switchToEventView(selectedEvent)
        }
    }

    override fun close() {
        if (parent is MainScreen) {
            parent.reload()
        }

        TAMClient.refreshCurrentMusicPack()
        client?.setScreen(parent)
    }

    override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        renderBackground(context)
        context?.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 12, Colors.WHITE)
        super.render(context, mouseX, mouseY, delta)
    }

    private fun positionContainers() {
        val gridWidget = GridWidget()
        gridWidget.mainPositioner
            .marginLeft(LEFT_MARGIN / 2)
            .marginRight(RIGHT_MARGIN / 2)
        val adder: GridWidget.Adder? = gridWidget.createAdder(2)

        if (eventView) {
            adder?.add(predicateViewWidget)
            adder?.add(eventViewWidget)
        }
        else {
            adder?.add(packStructureWidget)
            adder?.add(predicateViewWidget)
        }

        gridWidget.refreshPositions()
        SimplePositioningWidget.setPos(
            gridWidget, LEFT_MARGIN, TOP_MARGIN, RIGHT_MARGIN, BOTTOM_MARGIN, 0f, 0f)
    }

    private fun switchToEventView(event: MusicEvent?) {
        eventViewWidget.visible = true
        packStructureWidget.visible = false
        eventViewWidget.setEvent(event)
        positionContainers()
    }

    private fun switchToPredicateView() {
        packStructureWidget.visible = true
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
        private val CHECKMARK: Identifier = Identifier("minecraft", "textures/gui/checkmark.png")
        private const val TOP_MARGIN = 25
        private const val BOTTOM_MARGIN = TOP_MARGIN / 4
        private const val LEFT_MARGIN = TOP_MARGIN / 4
        private const val RIGHT_MARGIN = LEFT_MARGIN
        private val OPEN_ASSETS_TEXT = Text.literal("Show Assets")
        private val SAVE_BUTTON_TEXT = Text.literal("Save and Zip")
        private val CLOSE_BUTTON_TEXT = Text.literal("Close")
    }
}