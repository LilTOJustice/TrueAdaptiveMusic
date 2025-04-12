package liltojustice.trueadaptivemusic.client.sound

import liltojustice.trueadaptivemusic.client.sound.file.SoundFile
import liltojustice.trueadaptivemusic.client.sound.stream.FFMpegAudioStream
import net.minecraft.util.JsonHelper
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.sound.sampled.AudioFormat

class FFMpeg {
    companion object {
        fun makeStream(soundFile: SoundFile): FFMpegAudioStream {
            val ffprobe = ProcessBuilder(
                "ffprobe",
                "-hide_banner",
                "-i", "pipe:0",
                "-v", "panic",
                "-show_streams",
                "-select_streams", "0",
                "-print_format", "json")
                .start()

            // Ignore dumb exception
            try {
                soundFile.getInputStream().use {
                    it.copyTo(ffprobe.outputStream)
                }
            }
            catch (_: Exception) {}

            val reader = BufferedReader(InputStreamReader(ffprobe.inputStream))
            var line = ""
            val output = StringBuilder()
            while (reader.readLine()?.also { line = it } != null) {
                output.append(line)
            }

            ffprobe.waitFor()

            val propertyJson = JsonHelper.deserialize(output.toString())
            val stream = propertyJson["streams"].asJsonArray[0].asJsonObject
            val channels = stream["channels"].asInt
            val sampleRate = stream["sample_rate"].asInt

            return FFMpegAudioStream(
                soundFile,
                AudioFormat(
                    sampleRate.toFloat(),
                    16,
                    channels,
                    true,
                    false))
        }
    }
}