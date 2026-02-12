package liltojustice.trueadaptivemusic.client.sound.instance

import liltojustice.trueadaptivemusic.client.TAMClient
import liltojustice.trueadaptivemusic.client.music.pack.MusicLoadException
import liltojustice.trueadaptivemusic.client.sound.FFmpeg
import liltojustice.trueadaptivemusic.client.sound.file.SoundFile
import liltojustice.trueadaptivemusic.client.sound.stream.FFmpegAudioStream
import liltojustice.trueadaptivemusic.client.sound.stream.TruncatedAudioStream
import net.minecraft.client.sound.AudioStream
import net.minecraft.client.sound.OggAudioStream

class AudioFileSoundInstance(private val soundFile: SoundFile, isAmbient: Boolean): TAMSoundInstance(isAmbient) {
    val fileName
        get() = soundFile.getName().split('.').dropLast(1).joinToString(".")

    override fun getAudioStream(): AudioStream {
        val extension = soundFile.getExtension()
        try {
            return if (!TAMClient.hasFFmpeg && extension == "ogg") {
                    TruncatedAudioStream(OggAudioStream(soundFile.getInputStream()))
            } else {
                val loudnessUnits = if (isAmbient) AMBIENT_LUFS else MUSIC_LUFS
                TruncatedAudioStream(
                    FFmpegAudioStream(
                        soundFile,
                        FFmpeg.getFileAudioFormat(soundFile.getInputStream()),
                        loudnessUnits))
            }
        }
        catch (_: Exception) {
            throw MusicLoadException("Failed to play sound file '${soundFile.getName()}'")
        }
    }

    companion object {
        private const val AMBIENT_LUFS = -36
        private const val MUSIC_LUFS = -26
    }
}
