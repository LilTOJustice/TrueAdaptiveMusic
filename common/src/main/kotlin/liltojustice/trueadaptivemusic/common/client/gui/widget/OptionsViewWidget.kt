package liltojustice.trueadaptivemusic.common.client.gui.widget

import liltojustice.trueadaptivemusic.common.client.TrueAdaptiveMusicOptions
import liltojustice.trueadaptivemusic.common.client.gui.widget.utility.ContainerWidget
import liltojustice.trueadaptivemusicapi.TAMAPI
import liltojustice.trueadaptivemusicapi.widget.WidgetArg
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.narration.NarrationElementOutput
import kotlin.reflect.full.primaryConstructor

class OptionsViewWidget(initialOptions: TrueAdaptiveMusicOptions, width: Int, height: Int, x: Int = 0, y: Int = 0)
    : ContainerWidget(width, height, "", false, false, x = x, y = y) {
    private val requiredOptionsArgs = TrueAdaptiveMusicOptions
        .getRequiredArgs().map { WidgetArg.of(it) }
    private var optionsArgs: MutableList<Any?> = initialOptions.getArgs().toMutableList()

    override fun updateWidgetNarration(output: NarrationElementOutput) {
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