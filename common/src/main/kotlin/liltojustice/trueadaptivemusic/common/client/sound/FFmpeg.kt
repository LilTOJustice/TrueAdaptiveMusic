package liltojustice.trueadaptivemusic.common.client.sound

import liltojustice.trueadaptivemusic.common.client.TAMClient
import net.minecraft.util.GsonHelper
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import javax.sound.sampled.AudioFormat

object FFmpeg {
    fun getFileAudioFormat(inputStream: InputStream): AudioFormat {
        val ffprobe = ProcessBuilder(
            TAMClient.getFFProbeCommand(),
            "-hide_banner",
            "-i", "pipe:0",
            "-v", "panic",
            "-show_streams",
            "-select_streams", "0",
            "-print_format", "json"
        ).start()

        // Ignore dumb exception
        try {
            inputStream.use { it.copyTo(ffprobe.outputStream) }
        }
        catch (_: Exception) {}

        val output = StringBuilder()
        BufferedReader(InputStreamReader(ffprobe.inputStream)).use { reader ->
            var line = ""
            while (reader.readLine()?.also { line = it } != null) {
                output.append(line)
            }
        }

        ffprobe.waitFor()

        val propertyJson = GsonHelper.parse(output.toString())
        val stream = propertyJson["streams"].asJsonArray[0].asJsonObject
        val channels = stream["channels"].asInt
        val sampleRate = stream["sample_rate"].asInt

        return AudioFormat(sampleRate.toFloat(), 16, channels, true, false)
    }
}