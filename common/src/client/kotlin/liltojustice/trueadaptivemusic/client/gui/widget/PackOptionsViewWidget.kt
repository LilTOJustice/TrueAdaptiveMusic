package liltojustice.trueadaptivemusic.client.gui.widget

import liltojustice.trueadaptivemusic.client.gui.widget.utility.ContainerWidget
import liltojustice.trueadaptivemusic.client.music.pack.MusicPackOptions
import liltojustice.trueadaptivemusicapi.TAMAPI
import liltojustice.trueadaptivemusicapi.widget.WidgetArg
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.input.MouseButtonEvent
import kotlin.reflect.full.primaryConstructor

class PackOptionsViewWidget(initialOptions: MusicPackOptions, width: Int, height: Int, x: Int = 0, y: Int = 0)
    : ContainerWidget(width, height, "", false, false, x = x, y = y) {
    private val requiredOptionsArgs = MusicPackOptions
        .getRequiredArgs().map { WidgetArg.of(it) }
    private var optionsArgs: MutableList<Any?> = initialOptions.getArgs().toMutableList()

    override fun updateWidgetNarration(output: NarrationElementOutput) {
    }

    override fun mouseClicked(event: MouseButtonEvent, doubleClick: Boolean): Boolean {
        super.mouseClicked(event, doubleClick)

        return false
    }

    override fun extractWidgetRenderState(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, a: Float) {
        super.extractWidgetRenderState(graphics, mouseX, mouseY, a)
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