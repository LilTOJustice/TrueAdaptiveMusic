package liltojustice.trueadaptivemusic.client.gui.widget

import liltojustice.trueadaptivemusic.client.TrueAdaptiveMusicOptions
import liltojustice.trueadaptivemusic.client.gui.widget.utility.ContainerWidget
import liltojustice.trueadaptivemusicapi.TAMAPI
import liltojustice.trueadaptivemusicapi.widget.WidgetArg
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.narration.NarrationElementOutput
import kotlin.reflect.full.primaryConstructor

class OptionsViewWidget(initialOptions: TrueAdaptiveMusicOptions, width: Int, height: Int, x: Int = 0, y: Int = 0)
    : ContainerWidget(width, height, "", false, false, x = x, y = y) {
    private val requiredOptionsArgs = TrueAdaptiveMusicOptions
        .getRequiredArgs().map { WidgetArg.of(it) }
    private var optionsArgs: MutableList<Any?> = initialOptions.getArgs().toMutableList()

    override fun updateWidgetNarration(builder: NarrationElementOutput) {
    }

    override fun renderWidget(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
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
                        required.name?.let { TrueAdaptiveMusicOptions.getArgDisplayName(it) },
                        required.name?.let { TrueAdaptiveMusicOptions.getArgDescription(it) }
                    ) {}
                },
                "${required.name}: ${required.type}")
        }
    }

    fun getCurrentOptions(): TrueAdaptiveMusicOptions {
        return TrueAdaptiveMusicOptions::class.primaryConstructor?.call(*optionsArgs.toTypedArray())
            ?: TrueAdaptiveMusicOptions()
    }
}