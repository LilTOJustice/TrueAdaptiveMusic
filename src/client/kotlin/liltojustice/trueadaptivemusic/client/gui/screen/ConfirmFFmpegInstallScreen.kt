package liltojustice.trueadaptivemusic.client.gui.screen

import liltojustice.trueadaptivemusic.Logger
import liltojustice.trueadaptivemusic.client.TAMClient
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.TextIconButtonWidget
import net.minecraft.text.Text
import net.minecraft.util.Colors
import net.minecraft.util.Identifier
import kotlin.io.path.*

@Environment(EnvType.CLIENT)
class ConfirmFFmpegInstallScreen(private val parent: Screen)
    : Screen(Text.translatableWithFallback("trueadaptivemusic.ffmpeg_install", "Install FFmpeg?")) {
    @OptIn(ExperimentalPathApi::class)
    override fun init() {
        val acceptButtonWidget = TextIconButtonWidget.Builder(
            Text.translatableWithFallback("trueadaptivemusic.accept", "Accept"), {
                installFFmpeg()
                TAMClient.agreedToFFmpeg = true
                close() },
            false)
            .texture(CHECKMARK, 9, 8)
            .build()
        acceptButtonWidget.width = 60
        acceptButtonWidget.x = width / 2 - 32 - acceptButtonWidget.width / 2
        acceptButtonWidget.y = height / 2 + textRenderer.fontHeight * 2 + 10
        val backButtonWidget = ButtonWidget.Builder(
            Text.translatableWithFallback("trueadaptivemusic.go_back", "Go Back")) { close() }
                .build()
        backButtonWidget.width = 60
        backButtonWidget.x = width / 2 + 32 - backButtonWidget.width / 2
        backButtonWidget.y = height / 2 + textRenderer.fontHeight * 2 + 10

        addDrawableChild(acceptButtonWidget)
        addDrawableChild(backButtonWidget)
    }

    override fun close() {
        client?.setScreen(parent)
    }

    override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(context, mouseX, mouseY, delta)
        context?.drawCenteredTextWithShadow(
            client?.textRenderer,
            Text.translatableWithFallback(
                "trueadaptivemusic.ffmpeg_description",
                "FFmpeg is an open-source audio coding library that TrueAdaptiveMusic needs to decode any" +
                        " non-ogg files that certain packs may include."),
            width / 2,
            height / 2,
            Colors.WHITE)
        context?.drawCenteredTextWithShadow(
            client?.textRenderer,
            Text.translatableWithFallback(
                "trueadaptivemusic.ffmpeg_agree",
                "Do you agree to install FFmpeg and the package conditions as outlined on ffmpeg.org? " +
                        "P.S. You need to restart your PC after installing."),
            width / 2,
            height / 2 + textRenderer.fontHeight + 5,
            Colors.WHITE)
    }

    companion object {
        private val CHECKMARK: Identifier = Identifier.ofVanilla("icon/checkmark")
        fun installFFmpeg() {
            try {
                val ffmpegInstall =
                    ProcessBuilder(
                        "powershell.exe",
                        "-Command",
                        "winget install 'FFmpeg (Essentials Build)'",
                        "--accept-package-agreements",
                        "--accept-source-agreements")
                        .redirectErrorStream(true)
                        .start()

                val output = ffmpegInstall.inputStream.bufferedReader().use { it.readText() }
                ffmpegInstall.waitFor()

                if (ffmpegInstall.exitValue() != 0) {
                    Logger.logWarning(
                        "Failed to install ffmpeg with exit code ${ffmpegInstall.exitValue()}:\n${output}")
                }
            }
            catch (e: Exception) {
                Logger.logError("Failed to auto-install ffmpeg: ${e.message}")
            }
        }
    }
}