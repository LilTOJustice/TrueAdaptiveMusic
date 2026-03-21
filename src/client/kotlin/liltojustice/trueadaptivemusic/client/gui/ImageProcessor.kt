package liltojustice.trueadaptivemusic.client.gui

import liltojustice.trueadaptivemusic.Constants
import liltojustice.trueadaptivemusic.Logger
import liltojustice.trueadaptivemusic.client.TAMClient
import net.minecraft.client.texture.NativeImage
import java.io.InputStream
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.invariantSeparatorsPathString
import kotlin.io.path.pathString

object ImageProcessor {
    fun getNativeImage(filePath: Path): NativeImage? {
        return try {
            NativeImage.read(
                if (filePath.extension == "png")
                    filePath.toFile().inputStream()
                else if (TAMClient.hasFFmpeg)
                    convertToPNG(filePath)
                else
                    return null
            )
        }
        catch (e: Exception) {
            Logger.logError("Failed to load image at $filePath.\n${e.message}", true)

            null
        }
    }

    private fun convertToPNG(filePath: Path): InputStream {
        val command = if (TAMClient.hasFFmpegGlobal) "ffmpeg" else Constants.FFMPEG_PATH.pathString
        val ffmpeg = ProcessBuilder(
            command,
            "-i", filePath.invariantSeparatorsPathString,
            "-c:v", "png",
            "-f", "image2pipe",
            "-")
            .start()

        return ffmpeg.inputStream
    }
}