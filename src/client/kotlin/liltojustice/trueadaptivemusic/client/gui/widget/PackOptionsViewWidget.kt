package liltojustice.trueadaptivemusic.client.gui.widget

import liltojustice.trueadaptivemusic.client.gui.widget.utility.ContainerWidget
import liltojustice.trueadaptivemusic.client.music.pack.MusicPackOptions
import liltojustice.trueadaptivemusicapi.TAMAPI
import liltojustice.trueadaptivemusicapi.widget.WidgetArg
import net.minecraft.client.gui.Click
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import kotlin.reflect.full.primaryConstructor

class PackOptionsViewWidget(initialOptions: MusicPackOptions, width: Int, height: Int, x: Int = 0, y: Int = 0)
    : ContainerWidget(width, height, "", false, false, x = x, y = y) {
    private val requiredOptionsArgs = MusicPackOptions
        .getRequiredArgs().map { WidgetArg.of(it) }
    private var optionsArgs: MutableList<Any?> = initialOptions.getArgs().toMutableList()

    override fun appendClickableNarrations(builder: NarrationMessageBuilder?) {
    }

    override fun mouseClicked(click: Click, doubled: Boolean): Boolean {
        super.mouseClicked(click, doubled)
        return false
    }

    override fun renderWidget(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        super.renderWidget(context, mouseX, mouseY, delta)
        if (!visible) {
            return
        }

        requiredOptionsArgs.forEach { required ->
            addWidgetFromRender(
                {
                    TAMAPI.makeInputWidget(
                        screen!!,
                        optionsArgs,
                        required,
                        required.name?.let { MusicPackOptions.getArgDisplayName(it) },
                        required.name?.let { MusicPackOptions.getArgDescription(it) }
                    ) {}
                },
                "${required.name}: ${required.type}")
        }
    }

    fun getCurrentOptions(): MusicPackOptions {
        return MusicPackOptions::class.primaryConstructor?.call(*optionsArgs.toTypedArray())
            ?: MusicPackOptions()
    }
}