package liltojustice.trueadaptivemusic.client.sound.stream

import liltojustice.trueadaptivemusic.Constants
import liltojustice.trueadaptivemusic.client.TAMClient
import liltojustice.trueadaptivemusic.client.sound.file.SoundFile
import net.minecraft.client.sound.AudioStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.sound.sampled.AudioFormat
import kotlin.io.path.pathString

class FFmpegAudioStream(soundFile: SoundFile, private val format: AudioFormat): AudioStream {
    private val ffmpeg = run {
        val command = if (TAMClient.hasFFmpegGlobal) "ffmpeg" else Constants.FFMPEG_PATH.pathString
        val ffmpeg = ProcessBuilder(
            command,
            "-v", "panic",
            "-i", "pipe:0",
            "-f", "s16le",
            "-af", "loudnorm=I=-16",
            "-ar", "${format.sampleRate.toInt()}",
            "-acodec", "pcm_s16le",
            "-")
            .start()

        Thread() {
            try {
                soundFile.getInputStream().use {
                    it.copyTo(ffmpeg.outputStream)
                }
                ffmpeg.outputStream.close()
            }
            catch (_: Exception) {}
        }.start()

        ffmpeg
    }

    override fun close() {
        ffmpeg.destroy()
    }

    override fun getFormat(): AudioFormat {
        return format
    }

    override fun getBuffer(size: Int): ByteBuffer? {
        val bytes = ffmpeg.inputStream.readNBytes(size)
        val buffer = ByteBuffer.allocateDirect(bytes.size)
        buffer.order(ByteOrder.LITTLE_ENDIAN)
        buffer.put(bytes)
        return buffer.flip()
    }
}