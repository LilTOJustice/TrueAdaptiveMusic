package liltojustice.trueadaptivemusic.client.gui.widget

import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.Drawable
import net.minecraft.client.gui.Element
import net.minecraft.client.gui.ScreenRect
import net.minecraft.client.gui.Selectable
import net.minecraft.client.gui.screen.Screen.OPTIONS_BACKGROUND_TEXTURE
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.client.gui.widget.Widget
import java.util.function.Consumer

class PackStructureWidget(private var x: Int, private var y: Int, private var width: Int, private var height: Int)
    : Drawable, Element, Selectable, Widget {
    override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        println("Render")
        context?.setShaderColor(0.125f, 0.125f, 0.125f, 1.0f)
        context?.drawTexture(
            OPTIONS_BACKGROUND_TEXTURE,
            x,
            y,
            0F,
            0F,
            width,
            height,
            32,
            32
        )
        context?.setShaderColor(1f, 1f, 1f, 1f)
    }

    override fun setFocused(focused: Boolean) {
    }

    override fun isFocused(): Boolean {
        return true
    }

    override fun setX(x: Int) {
        this.x = x
    }

    override fun setY(y: Int) {
        this.y = y
    }

    override fun getX(): Int {
        return x
    }

    override fun getY(): Int {
        return y
    }

    override fun getWidth(): Int {
        return width
    }

    override fun getHeight(): Int {
        return height
    }

    override fun getNavigationFocus(): ScreenRect {
        return ScreenRect(0, 0, 0, 0)
    }

    override fun forEachChild(consumer: Consumer<ClickableWidget>?) {
    }

    override fun appendNarrations(builder: NarrationMessageBuilder?) {
    }

    override fun getType(): Selectable.SelectionType {
        return Selectable.SelectionType.NONE
    }
}