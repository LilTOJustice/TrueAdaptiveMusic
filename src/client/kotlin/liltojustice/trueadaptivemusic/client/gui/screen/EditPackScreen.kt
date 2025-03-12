package liltojustice.trueadaptivemusic.client.gui.screen

import liltojustice.trueadaptivemusic.client.Callbacks
import liltojustice.trueadaptivemusic.client.MusicPack
import liltojustice.trueadaptivemusic.client.gui.widget.PredicateTreeWidget
import liltojustice.trueadaptivemusic.client.gui.widget.PredicateViewWidget
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
class EditPackScreen(
    private val parent: Screen,
    private val musicPack: MusicPack)
    : Screen(Text.literal("Create/Edit a music pack")) {
    private lateinit var predicateViewWidget: PredicateViewWidget

    override fun init() {
        Callbacks.playSoundNow(null)
        musicPack.initEdit(musicPack)

        val saveButtonWidget = IconButtonWidget.Builder(SAVE_BUTTON_TEXT, CHECKMARK) {
            Callbacks.setCurrentMusicPack(null)
            val path = musicPack.save()
            Callbacks.setCurrentMusicPack(MusicPack.fromFile(path))
            this.close()
        }
            .iconSize(9, 8)
            .textureSize(9, 8)
            .xyOffset(32, 6)
            .build()
        saveButtonWidget.width = 90

        val closeButtonWidget = ButtonWidget.Builder(CLOSE_BUTTON_TEXT) {
            close()
        }
            .build()
        closeButtonWidget.x = saveButtonWidget.x + saveButtonWidget.width + 5
        closeButtonWidget.width = textRenderer.getWidth(CLOSE_BUTTON_TEXT) + 10
        closeButtonWidget.tooltip = Tooltip.of(Text.literal("Changes will be saved"))

        val openAssetsFolderButtonWidget = ButtonWidget.Builder(OPEN_ASSETS_TEXT) {
            Util.getOperatingSystem().open(musicPack.getEditPackAssetsPath().toUri())
        }
            .build()
        openAssetsFolderButtonWidget.width = textRenderer.getWidth(OPEN_ASSETS_TEXT) + 10
        openAssetsFolderButtonWidget.x = width - openAssetsFolderButtonWidget.width

        val gridWidget = GridWidget()
        gridWidget.mainPositioner
            .marginLeft(LEFT_MARGIN / 2)
            .marginRight(RIGHT_MARGIN / 2)
        val adder: GridWidget.Adder? = gridWidget.createAdder(3)

        lateinit var predicateTreeWidget: PredicateTreeWidget
        predicateViewWidget = PredicateViewWidget(
            (width * 0.5 - LEFT_MARGIN - RIGHT_MARGIN).toInt(),
            (height - TOP_MARGIN - BOTTOM_MARGIN),
            musicPack,
            { predicateTreeWidget.initPredicateWidgets() })
        predicateTreeWidget = PredicateTreeWidget(
            (width * 0.5 - LEFT_MARGIN - RIGHT_MARGIN).toInt(),
            (height - TOP_MARGIN - BOTTOM_MARGIN),
            musicPack,
            { node -> predicateViewWidget.setEditExistingNode(node) },
            { parent -> predicateViewWidget.setCreateNewNode(parent) })
        adder?.add(predicateTreeWidget, 2)
        adder?.add(predicateViewWidget, 1)

        gridWidget.refreshPositions()
        SimplePositioningWidget.setPos(
            gridWidget, LEFT_MARGIN, TOP_MARGIN, RIGHT_MARGIN, BOTTOM_MARGIN, 0f, 0f)
        addDrawableChild(saveButtonWidget)
        addDrawableChild(closeButtonWidget)
        addDrawableChild(openAssetsFolderButtonWidget)
        gridWidget.forEachChild { drawableElement: ClickableWidget? ->
            this.addDrawableChild(
                drawableElement
            )
        }
    }

    override fun close() {
        if (parent is MainScreen) {
            parent.reload()
        }

        Callbacks.refreshCurrentMusicPack()
        client?.setScreen(parent)
    }

    override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        renderBackground(context)
        context?.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 12, Colors.WHITE)
        super.render(context, mouseX, mouseY, delta)
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