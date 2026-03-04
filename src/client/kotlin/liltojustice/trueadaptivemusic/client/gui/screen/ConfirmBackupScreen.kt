package liltojustice.trueadaptivemusic.client.gui.screen

import liltojustice.trueadaptivemusic.Logger
import liltojustice.trueadaptivemusic.client.TAMClient
import liltojustice.trueadaptivemusic.client.music.pack.MusicPack
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.TextIconButtonWidget
import net.minecraft.text.Text
import net.minecraft.util.Colors
import net.minecraft.util.Identifier
import java.nio.file.Path
import kotlin.io.path.*

@Environment(EnvType.CLIENT)
class ConfirmBackupScreen(
    private val parent: Screen, private val backupPath: Path, private val deleteDestination: Screen)
    : Screen(Text.translatableWithFallback("trueadaptivemusic.backup_exists", "Backup Exists")) {
    @OptIn(ExperimentalPathApi::class)
    override fun init() {
        val acceptButtonWidget = TextIconButtonWidget.Builder(
            Text.translatableWithFallback("trueadaptivemusic.keep", "Keep"), {
                val backup = MusicPack.fromFile(backupPath)
                TAMClient.musicPack = backup
                TAMClient.musicPack?.let {
                    client?.setScreen(EditPackScreen(parent, it))
                } ?: run {
                    Logger.logError("Failed to load existing pack.")
                }
        }, false)
            .texture(CHECKMARK, 9, 8)
            .build()
        val deleteButtonWidget = ButtonWidget.Builder(
            Text.translatableWithFallback("trueadaptivemusic.delete", "Delete")) {
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
        renderBackground(context, mouseX, mouseY, delta)
        super.render(context, mouseX, mouseY, delta)
        context?.drawCenteredTextWithShadow(
            client?.textRenderer,
            Text.translatableWithFallback(
                "trueadaptivemusic.existing_edit", "Unsaved pack edit $backupPath already exists."),
            width / 2,
            height / 2,
            Colors.WHITE)
        context?.drawCenteredTextWithShadow(
            client?.textRenderer,
            Text.translatableWithFallback(
                "trueadaptivemusic.continue_edit",
                "Do you want to keep and continue editing it, or delete it and continue?"),
            width / 2,
            height / 2 + textRenderer.fontHeight + 5,
            Colors.WHITE)
    }

    companion object {
        private val CHECKMARK: Identifier = Identifier.ofVanilla("icon/checkmark")
    }
}