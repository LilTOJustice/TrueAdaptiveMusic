package liltojustice.trueadaptivemusic.client.gui.widget.utility

import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.screen.ScreenTexts

fun makeDoneButton(textRenderer: TextRenderer, screenWidth: Int, screenHeight: Int, action: () -> Unit): ButtonWidget {
    val doneButton = ButtonWidget.builder(ScreenTexts.DONE) { action() }.build()
    doneButton.width = textRenderer.getWidth(ScreenTexts.DONE) + 10
    doneButton.x = screenWidth - doneButton.width - 1
    doneButton.y = screenHeight - doneButton.height - 2

    return doneButton
}