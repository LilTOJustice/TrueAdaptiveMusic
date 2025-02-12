package liltojustice.trueadaptivemusic.client.gui.screen

import liltojustice.trueadaptivemusic.Logger
import liltojustice.trueadaptivemusic.client.gui.widget.PackStructureWidget
import liltojustice.trueadaptivemusic.client.gui.widget.PredicateViewWidget
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.client.gui.widget.GridWidget
import net.minecraft.client.gui.widget.IconButtonWidget
import net.minecraft.client.gui.widget.SimplePositioningWidget
import net.minecraft.text.Text
import net.minecraft.util.Identifier

@Environment(EnvType.CLIENT)
class EditPackScreen(private val parent: Screen): Screen(Text.literal("Create/Edit a music pack")) {
    override fun init() {
        val saveButtonWidget = IconButtonWidget.Builder(Text.literal("Save"), CHECKMARK)
        { Logger.log("Save pack clicked") }
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
        val packStructureWidget = PackStructureWidget(
            width = (width * 0.66f - LEFT_MARGIN - RIGHT_MARGIN).toInt(),
            height = (height - TOP_MARGIN - BOTTOM_MARGIN),
            true)
        val predicateViewWidget = PredicateViewWidget(
            width = (width * 0.33 - LEFT_MARGIN - RIGHT_MARGIN).toInt(),
            height = (height - TOP_MARGIN - BOTTOM_MARGIN),
            true)
        packStructureWidget.onSelectPredicate { predicate -> predicateViewWidget.setPredicate(predicate) }
        adder?.add(packStructureWidget, 2)
        adder?.add(predicateViewWidget, 1)

        gridWidget.refreshPositions()
        SimplePositioningWidget.setPos(
            gridWidget, LEFT_MARGIN, TOP_MARGIN, RIGHT_MARGIN, BOTTOM_MARGIN, 0f, 0f)
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