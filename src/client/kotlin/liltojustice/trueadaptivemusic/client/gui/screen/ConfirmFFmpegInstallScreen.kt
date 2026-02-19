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
class ConfirmFFmpegInstallScreen(private val parent: Screen)
    : Screen(
    Text.translatableWithFallback(
        "trueadaptivemusic.ffmpeg_install", "Install FFmpeg").append("?")
    ) {
    @OptIn(ExperimentalPathApi::class)
    override fun init() {
        val acceptButtonWidget = TextIconButtonWidget.Builder(
            ScreenTexts.PROCEED,
            {
                Util.getOperatingSystem().open(Constants.FFMPEG_DOWNLOAD_LINK)
                close()
            },
            false)
            .texture(CHECKMARK, 9, 8)
            .build()
        val backButtonWidget = ButtonWidget.Builder(ScreenTexts.BACK) { close() }.build()
        acceptButtonWidget.width = textRenderer.getWidth(acceptButtonWidget.message) + 20
        backButtonWidget.width = textRenderer.getWidth(backButtonWidget.message) + 10
        acceptButtonWidget.x = width / 2 - 32 - acceptButtonWidget.width / 2
        acceptButtonWidget.y = height / 2 + textRenderer.fontHeight * 2 + 10
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
                "trueadaptivemusic.ffmpeg_instruction",
                "ffmpeg.exe and ffprobe.exe should be placed in").string
                    + ' ' + Constants.OPTIONS_DIR.pathString,
            width / 2,
            height / 2,
            Colors.WHITE)
        context?.drawCenteredTextWithShadow(
            client?.textRenderer,
            Text.translatableWithFallback(
                "trueadaptivemusic.ffmpeg_agree", "Would you like to open the link to download FFmpeg?"),
            width / 2,
            height / 2 + textRenderer.fontHeight + 5,
            Colors.WHITE)
    }

    companion object {
        private val CHECKMARK: Identifier = Identifier.ofVanilla("icon/checkmark")
    }
}