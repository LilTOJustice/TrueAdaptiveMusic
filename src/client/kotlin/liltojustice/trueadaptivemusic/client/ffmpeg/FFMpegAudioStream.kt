package liltojustice.trueadaptivemusic.client.ffmpeg

import liltojustice.trueadaptivemusic.client.sound.SoundFile
import net.minecraft.client.sound.AudioStream
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.sound.sampled.AudioFormat

class FFMpegAudioStream(soundFile: SoundFile, private val format: AudioFormat): AudioStream {
    private val inputStream = soundFile.getInputStream().use {
        val tempFile = File.createTempFile("tam", ".tmp")
        tempFile.writeBytes(it.readAllBytes())
        tempFile.deleteOnExit()
        val ffmpeg = ProcessBuilder(
            "ffmpeg",
            "-v", "panic",
            "-i", tempFile.absolutePath,
            "-f", "s16le",
            "-acodec", "pcm_s16le",
            "-")
            .start()

        ffmpeg.inputStream
    }

    override fun close() {
        inputStream.close()
    }

    override fun getFormat(): AudioFormat {
        return format
    }

    override fun getBuffer(size: Int): ByteBuffer {
        val bytes = inputStream.readNBytes(size)
        val buffer = ByteBuffer.allocate(bytes.size)
        buffer.order(ByteOrder.LITTLE_ENDIAN)
        buffer.put(bytes)
        return buffer
    }
}