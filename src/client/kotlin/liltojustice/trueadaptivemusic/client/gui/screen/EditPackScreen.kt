package liltojustice.trueadaptivemusic.client.gui.screen

import liltojustice.trueadaptivemusic.client.MusicPack
import liltojustice.trueadaptivemusic.client.gui.widget.PredicateTreeWidget
import liltojustice.trueadaptivemusic.client.gui.widget.PredicateViewWidget
import liltojustice.trueadaptivemusic.client.predicate.MusicPredicate
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.*
import net.minecraft.text.Text
import net.minecraft.util.Identifier

@Environment(EnvType.CLIENT)
class EditPackScreen(
    private val parent: Screen,
    private val musicPack: MusicPack,
    private val isBkp: Boolean = false)
    : Screen(Text.literal("Create/Edit a music pack")) {
    override fun init() {
        musicPack.initEdit(isBkp)

        val saveButtonWidget = IconButtonWidget.Builder(Text.literal("Save"), CHECKMARK) {
            musicPack.save()
            this.close()
        }
            .iconSize(9, 8)
            .textureSize(9, 8)
            .xyOffset(13, 6)
            .build()
        saveButtonWidget.width = 50

        val gridWidget = GridWidget()
        gridWidget.mainPositioner
            .marginLeft(LEFT_MARGIN / 2)
            .marginRight(RIGHT_MARGIN / 2)
        val adder: GridWidget.Adder? = gridWidget.createAdder(3)

        val predicateViewWidget = PredicateViewWidget(this,
            (width * 0.33 - LEFT_MARGIN - RIGHT_MARGIN).toInt(),
            (height - TOP_MARGIN - BOTTOM_MARGIN))
        val predicateTreeWidget = PredicateTreeWidget(this,
            (width * 0.66f - LEFT_MARGIN - RIGHT_MARGIN).toInt(),
            (height - TOP_MARGIN - BOTTOM_MARGIN),
            { predicate: MusicPredicate -> predicateViewWidget.setPredicate(predicate) },
            musicPack)
        adder?.add(predicateTreeWidget, 2)
        adder?.add(predicateViewWidget, 1)

        gridWidget.refreshPositions()
        SimplePositioningWidget.setPos(
            gridWidget, LEFT_MARGIN, TOP_MARGIN, RIGHT_MARGIN, BOTTOM_MARGIN, 0f, 0f)
        predicateTreeWidget.refreshPositions()
        predicateViewWidget.refreshPositions()
        addDrawableChild(saveButtonWidget)
        gridWidget.forEachChild { drawableElement: ClickableWidget? ->
            this.addDrawableChild(
                drawableElement
            )
        }
    }

    override fun close() {
        client?.setScreen(parent)
    }

    override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        renderBackground(context)
        context?.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 12, 16777215)
        super.render(context, mouseX, mouseY, delta)
    }

    companion object {
        private val CHECKMARK: Identifier = Identifier("minecraft", "textures/gui/checkmark.png")
        private const val TOP_MARGIN = 25
        private const val BOTTOM_MARGIN = TOP_MARGIN / 4
        private const val LEFT_MARGIN = TOP_MARGIN / 4
        private const val RIGHT_MARGIN = LEFT_MARGIN
    }
}