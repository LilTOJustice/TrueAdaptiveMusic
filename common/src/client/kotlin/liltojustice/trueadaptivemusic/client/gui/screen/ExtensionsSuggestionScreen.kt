package liltojustice.trueadaptivemusic.client.gui.screen

import liltojustice.trueadaptivemusic.Constants
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.SpriteIconButton
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.util.CommonColors
import net.minecraft.util.Util
import kotlin.io.path.*

class ExtensionsSuggestionScreen(private val parent: Screen)
    : Screen(
    Component.translatableWithFallback(
        "trueadaptivemusic.extensions_suggested", "TAM Extensions Mod Recommended")
    ) {
    @OptIn(ExperimentalPathApi::class)
    override fun init() {
        val yesButtonWidget = SpriteIconButton.builder(
            CommonComponents.GUI_YES,
            {
                Util.getPlatform().openUri(Constants.TAM_EXTENSIONS_URL)
                minecraft.gui.setScreen(parent)
            },
            false
        )
            .sprite(CHECKMARK, 9, 8)
            .build()
        val noButtonWidget = Button.Builder(CommonComponents.GUI_NO) {
            minecraft.gui.setScreen(parent)
        }
            .build()
        yesButtonWidget.width = 60
        noButtonWidget.width = 60
        yesButtonWidget.x = width / 2 - 32 - yesButtonWidget.width / 2
        noButtonWidget.x = width / 2 + 32 - noButtonWidget.width / 2
        yesButtonWidget.y = height / 2 + (font.lineHeight + 5) * 2
        noButtonWidget.y = yesButtonWidget.y

        addRenderableWidget(yesButtonWidget)
        addRenderableWidget(noButtonWidget)
    }

    override fun onClose() {
        minecraft.gui.setScreen(parent)
    }

    override fun extractRenderState(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, a: Float) {
        super.extractRenderState(graphics, mouseX, mouseY, a)
        graphics.centeredText(
            font,
            Component.translatableWithFallback(
                "trueadaptivemusic.extensions_suggested_1",
                "The True Adaptive Music Extensions mod is required for this, but wasn't found."
            ),
            width / 2,
            height / 2 - (font.lineHeight + 5),
            CommonColors.WHITE
        )
        graphics.centeredText(
            font,
            Component.translatableWithFallback(
                "trueadaptivemusic.extensions_suggested_2",
                "This is usually because you got TAM from CurseForge, which doesn't support it."
            ),
            width / 2,
            height / 2,
            CommonColors.WHITE
        )
        graphics.centeredText(
            font,
            Component.translatableWithFallback(
                "trueadaptivemusic.extensions_suggested_2",
                "Do you want to open the mod page for TAM Extensions on Modrinth?"
            ),
            width / 2,
            height / 2 + font.lineHeight + 5,
            CommonColors.WHITE
        )
    }

    companion object {
        private val CHECKMARK: Identifier = Identifier.withDefaultNamespace("icon/checkmark")
    }
}