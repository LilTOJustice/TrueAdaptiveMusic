package liltojustice.trueadaptivemusic.client.gui.widget.utility

import net.minecraft.client.gui.Font
import net.minecraft.client.gui.components.Button
import net.minecraft.network.chat.CommonComponents

fun makeDoneButton(font: Font, screenWidth: Int, screenHeight: Int, action: () -> Unit): Button {
    val doneButton = Button.builder(CommonComponents.GUI_DONE) { action() }.build()
    doneButton.width = font.width(CommonComponents.GUI_DONE) + 10
    doneButton.x = screenWidth - doneButton.width - 1
    doneButton.y = screenHeight - doneButton.height - 2

    return doneButton
}