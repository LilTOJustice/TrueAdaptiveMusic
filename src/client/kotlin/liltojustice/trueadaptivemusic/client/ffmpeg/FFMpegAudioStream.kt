package liltojustice.trueadaptivemusic.client.ffmpeg

import liltojustice.trueadaptivemusic.client.sound.SoundFile
import net.minecraft.client.sound.AudioStream
import java.io.File
import java.nio.ByteBuffer
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
            "-probesize", "8192",
            "-")
            .start()

        val resTempFile = File.createTempFile("tamres", ".tmp")

        //resTempFile.writeBytes(ffmpeg.inputStream.readAllBytes())

        ffmpeg.inputStream
    }

    override fun close() {
        inputStream.close()
    }

    override fun getFormat(): AudioFormat {
        return format
    }

    override fun getBuffer(size: Int): ByteBuffer {
        return ByteBuffer.wrap(inputStream.readNBytes(size)).flip()
    }
}