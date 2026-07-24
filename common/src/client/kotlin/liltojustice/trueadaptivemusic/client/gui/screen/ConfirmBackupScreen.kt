package liltojustice.trueadaptivemusic.client.gui.screen

import liltojustice.trueadaptivemusic.Logger
import liltojustice.trueadaptivemusic.client.TAMClient
import liltojustice.trueadaptivemusic.client.music.pack.MusicPack
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.TextAndImageButton
import net.minecraft.network.chat.Component
import net.minecraft.util.CommonColors
import net.minecraft.resources.ResourceLocation
import java.nio.file.Path
import kotlin.io.path.*

class ConfirmBackupScreen(
    private val parent: Screen, private val backupPath: Path, private val deleteDestination: Screen
)
    : Screen(Component.translatableWithFallback("trueadaptivemusic.backup_exists", "Backup Exists")) {
    @OptIn(ExperimentalPathApi::class)
    override fun init() {
        val acceptButtonWidget = TextAndImageButton.Builder(
            Component.translatableWithFallback("trueadaptivemusic.keep", "Keep"), CHECKMARK) {
            val backup = MusicPack.fromFile(backupPath)
            TAMClient.musicPack = backup
            TAMClient.musicPack?.let {
                minecraft?.setScreen(EditPackScreen(parent, it))
            } ?: run {
                Logger.logError("Failed to load existing pack.")
            }
        }
            .usedTextureSize(9, 8)
            .textureSize(9, 8)
            .offset(16, 6)
            .build()
        val deleteButtonWidget = Button.Builder(
            Component.translatableWithFallback("trueadaptivemusic.delete", "Delete")) {
            backupPath.deleteRecursively()
            minecraft?.setScreen(deleteDestination)
        }
            .build()
        acceptButtonWidget.width = 60
        deleteButtonWidget.width = 60
        acceptButtonWidget.x = width / 2 - 32 - acceptButtonWidget.width / 2
        deleteButtonWidget.x = width / 2 + 32 - deleteButtonWidget.width / 2
        acceptButtonWidget.y = height / 2 + font.lineHeight * 2 + 10
        deleteButtonWidget.y = height / 2 + font.lineHeight * 2 + 10

        addRenderableWidget(acceptButtonWidget)
        addRenderableWidget(deleteButtonWidget)
    }

    override fun onClose() {
        minecraft?.setScreen(parent)
    }

    override fun render(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        renderBackground(context)
        context.drawCenteredString(
            minecraft!!.font,
            Component.translatableWithFallback(
                "trueadaptivemusic.existing_edit", "Unsaved pack edit $backupPath already exists."),
            width / 2,
            height / 2,
            CommonColors.WHITE)
        context.drawCenteredString(
            minecraft!!.font,
            Component.translatableWithFallback(
                "trueadaptivemusic.continue_edit",
                "Do you want to keep and continue editing it, or delete it and continue?"),
            width / 2,
            height / 2 + font.lineHeight + 5,
            CommonColors.WHITE)
        super.render(context, mouseX, mouseY, delta)
    }

    companion object {
        private val CHECKMARK: ResourceLocation = ResourceLocation("minecraft", "textures/gui/checkmark.png")
    }
}