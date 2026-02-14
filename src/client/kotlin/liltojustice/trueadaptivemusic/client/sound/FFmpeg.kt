package liltojustice.trueadaptivemusic.client.sound

import liltojustice.trueadaptivemusic.Constants
import liltojustice.trueadaptivemusic.client.TAMClient
import net.minecraft.util.JsonHelper
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import javax.sound.sampled.AudioFormat
import kotlin.io.path.pathString

object FFmpeg {
    fun getFileAudioFormat(inputStream: InputStream): AudioFormat {
        val command = if (TAMClient.hasFFmpegGlobal) "ffprobe" else Constants.FFPROBE_PATH.pathString
        val ffprobe = ProcessBuilder(
            command,
            "-hide_banner",
            "-i", "pipe:0",
            "-v", "panic",
            "-show_streams",
            "-select_streams", "0",
            "-print_format", "json")
            .start()

        // Ignore dumb exception
        try {
            inputStream.use { it.copyTo(ffprobe.outputStream) }
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

        return AudioFormat(sampleRate.toFloat(), 16, channels, true, false)
    }
}