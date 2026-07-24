package liltojustice.trueadaptivemusic.client.gui.screen

import liltojustice.trueadaptivemusic.Constants
import net.minecraft.Util
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.SpriteIconButton
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.CommonColors
import kotlin.io.path.*

class ExtensionsSuggestionScreen(private val parent: Screen)
    : Screen(
    Component.translatableWithFallback(
        "trueadaptivemusic.extensions_suggested", "TAM Extensions Missing")
    ) {
    @OptIn(ExperimentalPathApi::class)
    override fun init() {
        val yesButtonWidget = SpriteIconButton.builder(
            CommonComponents.GUI_YES,
            {
                Util.getPlatform().openUri(Constants.TAM_MODRINTH_URL)
                minecraft!!.setScreen(parent)
            },
            false
        )
            .sprite(CHECKMARK, 9, 8)
            .build()
        val noButtonWidget = Button.Builder(CommonComponents.GUI_NO) {
            minecraft!!.setScreen(parent)
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
        minecraft!!.setScreen(parent)
    }

    override fun render(graphics: GuiGraphics, mouseX: Int, mouseY: Int, a: Float) {
        super.render(graphics, mouseX, mouseY, a)
        graphics.drawCenteredString(
            font,
            Component.translatableWithFallback(
                "trueadaptivemusic.extensions_suggested_1",
                "The Modrinth version of TAM is required for this."
            ),
            width / 2,
            height / 2 - (font.lineHeight + 5),
            CommonColors.WHITE
        )
        graphics.drawCenteredString(
            font,
            Component.translatableWithFallback(
                "trueadaptivemusic.extensions_suggested_2",
                "This is usually because you got TAM from CurseForge."
            ),
            width / 2,
            height / 2,
            CommonColors.WHITE
        )
        graphics.drawCenteredString(
            font,
            Component.translatableWithFallback(
                "trueadaptivemusic.extensions_suggested_2",
                "Do you want to open the mod page for the Modrinth version?"
            ),
            width / 2,
            height / 2 + font.lineHeight + 5,
            CommonColors.WHITE
        )
    }

    companion object {
        private val CHECKMARK: ResourceLocation = ResourceLocation.withDefaultNamespace("icon/checkmark")
    }
}