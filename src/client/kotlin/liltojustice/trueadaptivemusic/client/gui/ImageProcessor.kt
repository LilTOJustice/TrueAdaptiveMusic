package liltojustice.trueadaptivemusic.client.gui

import liltojustice.trueadaptivemusic.Logger
import liltojustice.trueadaptivemusic.client.TAMClient
import net.minecraft.client.texture.NativeImage
import java.io.InputStream
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.invariantSeparatorsPathString

object ImageProcessor {
    fun getNativeImage(filePath: Path): NativeImage? {
        return try {
            NativeImage.read(
                if (filePath.extension == "png") {
                    filePath.toFile().inputStream()
                }
                else {
                    convertToPNG(filePath)
                }
            )
        }
        catch (e: Exception) {
            Logger.logError("Failed to load image at $filePath.\n${e.message}")

            null
        }
    }

    private fun convertToPNG(filePath: Path): InputStream {
        val ffmpeg = ProcessBuilder(
            TAMClient.getFFmpegCommand(),
            "-i", filePath.invariantSeparatorsPathString,
            "-c:v", "png",
            "-f", "image2pipe",
            "-")
            .start()

        return ffmpeg.inputStream
    }
}