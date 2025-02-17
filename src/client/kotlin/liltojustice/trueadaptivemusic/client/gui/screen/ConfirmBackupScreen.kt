package liltojustice.trueadaptivemusic.client.gui.screen

import liltojustice.trueadaptivemusic.client.MusicPack
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.IconButtonWidget
import net.minecraft.text.Text
import net.minecraft.util.Colors
import net.minecraft.util.Identifier
import java.nio.file.Path
import kotlin.io.path.*

@Environment(EnvType.CLIENT)
class ConfirmBackupScreen(
    private val parent: Screen, private val backupPath: Path, private val deleteDestination: Screen)
    : Screen(Text.literal("Backup Exists")) {
    @OptIn(ExperimentalPathApi::class)
    override fun init() {
        val acceptButtonWidget = IconButtonWidget.Builder(Text.literal("Keep"), CHECKMARK) {
            client?.setScreen(EditPackScreen(parent, MusicPack.fromFile(backupPath)))
        }
            .iconSize(9, 8)
            .textureSize(9, 8)
            .xyOffset(16, 6)
            .build()
        val deleteButtonWidget = ButtonWidget.Builder(Text.literal("Delete")) {
            backupPath.deleteRecursively()
            client?.setScreen(deleteDestination)
        }
            .build()
        acceptButtonWidget.width = 60
        deleteButtonWidget.width = 60
        acceptButtonWidget.x = width / 2 - 32 - acceptButtonWidget.width / 2
        deleteButtonWidget.x = width / 2 + 32 - deleteButtonWidget.width / 2
        acceptButtonWidget.y = height / 2 + textRenderer.fontHeight * 2 + 10
        deleteButtonWidget.y = height / 2 + textRenderer.fontHeight * 2 + 10

        addDrawableChild(acceptButtonWidget)
        addDrawableChild(deleteButtonWidget)
    }

    override fun close() {
        client?.setScreen(parent)
    }

    override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        renderBackground(context)
        context?.drawCenteredTextWithShadow(
            client?.textRenderer,
            "Unsaved pack edit $backupPath already exists.",
            width / 2,
            height / 2,
            Colors.WHITE)
        context?.drawCenteredTextWithShadow(
            client?.textRenderer,
            "Do you want to keep and continue editing it, or delete it and continue?",
            width / 2,
            height / 2 + textRenderer.fontHeight + 5,
            Colors.WHITE)
        super.render(context, mouseX, mouseY, delta)
    }

    companion object {
        private val CHECKMARK: Identifier = Identifier("minecraft", "textures/gui/checkmark.png")
    }
}