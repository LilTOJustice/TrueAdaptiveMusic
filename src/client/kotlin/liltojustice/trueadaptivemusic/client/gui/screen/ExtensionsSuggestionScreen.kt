package liltojustice.trueadaptivemusic.client.gui.screen

import liltojustice.trueadaptivemusic.Constants
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.TextIconButtonWidget
import net.minecraft.screen.ScreenTexts
import net.minecraft.text.Text
import net.minecraft.util.Colors
import net.minecraft.util.Identifier
import net.minecraft.util.Util
import kotlin.io.path.*

@Environment(EnvType.CLIENT)
class ExtensionsSuggestionScreen(private val parent: Screen)
    : Screen(
    Text.translatableWithFallback(
        "trueadaptivemusic.extensions_suggested", "TAM Extensions Mod Recommended")
    ) {
    @OptIn(ExperimentalPathApi::class)
    override fun init() {
        val yesButtonWidget = TextIconButtonWidget.builder(
            ScreenTexts.YES,
            {
                Util.getOperatingSystem().open(Constants.TAM_EXTENSIONS_URL)
                client.setScreen(parent)
            },
            false
        )
            .texture(CHECKMARK, 9, 8)
            .build()
        val noButtonWidget = ButtonWidget.Builder(ScreenTexts.NO) {
            client.setScreen(parent)
        }
            .build()
        yesButtonWidget.width = 60
        noButtonWidget.width = 60
        yesButtonWidget.x = width / 2 - 32 - yesButtonWidget.width / 2
        noButtonWidget.x = width / 2 + 32 - noButtonWidget.width / 2
        yesButtonWidget.y = height / 2 + (textRenderer.fontHeight + 5) * 2
        noButtonWidget.y = yesButtonWidget.y

        addDrawableChild(yesButtonWidget)
        addDrawableChild(noButtonWidget)
    }

    override fun close() {
        client.setScreen(parent)
    }

    override fun render(graphics: DrawContext, mouseX: Int, mouseY: Int, a: Float) {
        super.render(graphics, mouseX, mouseY, a)
        graphics.drawCenteredTextWithShadow(
            textRenderer,
            Text.translatableWithFallback(
                "trueadaptivemusic.extensions_suggested_1",
                "The True Adaptive Music Extensions mod is required for this, but wasn't found."
            ),
            width / 2,
            height / 2 - (textRenderer.fontHeight + 5),
            Colors.WHITE
        )
        graphics.drawCenteredTextWithShadow(
            textRenderer,
            Text.translatableWithFallback(
                "trueadaptivemusic.extensions_suggested_2",
                "This is usually because you got TAM from CurseForge, which doesn't support it."
            ),
            width / 2,
            height / 2,
            Colors.WHITE
        )
        graphics.drawCenteredTextWithShadow(
            textRenderer,
            Text.translatableWithFallback(
                "trueadaptivemusic.extensions_suggested_2",
                "Do you want to open the mod page for TAM Extensions on Modrinth?"
            ),
            width / 2,
            height / 2 + textRenderer.fontHeight + 5,
            Colors.WHITE
        )
    }

    companion object {
        private val CHECKMARK: Identifier = Identifier.ofVanilla("icon/checkmark")
    }
}