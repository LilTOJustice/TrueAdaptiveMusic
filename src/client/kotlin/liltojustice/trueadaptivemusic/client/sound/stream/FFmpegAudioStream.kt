package liltojustice.trueadaptivemusic.client.sound.stream

import liltojustice.trueadaptivemusic.Constants
import liltojustice.trueadaptivemusic.client.TAMClient
import net.minecraft.client.sound.AudioStream
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.sound.sampled.AudioFormat
import kotlin.io.path.pathString

class FFmpegAudioStream(inputStream: InputStream, private val format: AudioFormat, loudnessUnits: Int): AudioStream {
    private lateinit var thread: Thread
    private val ffmpeg = run {
        val command = if (TAMClient.hasFFmpegGlobal) "ffmpeg" else Constants.FFMPEG_PATH.pathString
        val ffmpeg = ProcessBuilder(
            command,
            "-v", "panic",
            "-i", "pipe:0",
            "-f", "s16le",
            "-af", "loudnorm=I=${loudnessUnits}",
            "-ar", "${format.sampleRate.toInt()}",
            "-acodec", "pcm_s16le",
            "-")
            .start()

        thread = Thread() {
            try {
                inputStream.use {
                    it.copyTo(ffmpeg.outputStream)
                }
            }
            catch (_: Exception) {
                ffmpeg.destroy()
            }
            finally {
                ffmpeg.outputStream.close()
            }
        }
        thread.name = "FFmpeg stream handler: ${inputStream.hashCode()}"
        thread.start()

        ffmpeg
    }

    override fun close() {
        ffmpeg.destroy()
        thread.interrupt()
        thread.join()
    }

    override fun getFormat(): AudioFormat {
        return format
    }

    override fun read(size: Int): ByteBuffer? {
        val bytes = ffmpeg.inputStream.readNBytes(size)
        val buffer = ByteBuffer.allocateDirect(bytes.size)
        buffer.order(ByteOrder.LITTLE_ENDIAN)
        buffer.put(bytes)
        return buffer.flip()
    }
}