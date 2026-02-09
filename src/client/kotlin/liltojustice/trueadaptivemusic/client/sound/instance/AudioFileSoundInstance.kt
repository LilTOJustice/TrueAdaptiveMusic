package liltojustice.trueadaptivemusic.client.sound.instance

import liltojustice.trueadaptivemusic.Constants
import liltojustice.trueadaptivemusic.client.TAMClient
import liltojustice.trueadaptivemusic.client.music.pack.MusicLoadException
import liltojustice.trueadaptivemusic.client.sound.file.SoundFile
import liltojustice.trueadaptivemusic.client.sound.stream.FFmpegAudioStream
import liltojustice.trueadaptivemusic.client.sound.stream.TruncatedAudioStream
import net.minecraft.client.sound.*
import net.minecraft.sound.SoundCategory
import net.minecraft.util.Identifier
import java.util.concurrent.CompletableFuture

class AudioFileSoundInstance(private val soundFile: SoundFile, private val isAmbient: Boolean)
    : AbstractSoundInstance(
    Constants.AUDIO_FILE_STREAM_ID,
    if (isAmbient) SoundCategory.AMBIENT else SoundCategory.MUSIC,
    SoundInstance.createRandom()),
    VolumeControlled {
    val fileName
        get() = soundFile.getName().split('.').dropLast(1).joinToString(".")

    override fun getAudioStream(loader: SoundLoader, id: Identifier, repeatInstantly: Boolean):
            CompletableFuture<AudioStream> {
        val extension = soundFile.getExtension()
        try {
            return if (!TAMClient.hasFFmpeg && extension == "ogg") {
                CompletableFuture.supplyAsync {
                    TruncatedAudioStream(OggAudioStream(soundFile.getInputStream())) }
            } else {
                CompletableFuture.supplyAsync {
                    val loudnessUnits = if (isAmbient) AMBIENT_LUFS else MUSIC_LUFS
                    TruncatedAudioStream(
                        FFmpegAudioStream(soundFile, soundFile.getAudioFormat(), loudnessUnits))
                }
            }
        }
        catch (_: Exception) {
            throw MusicLoadException("Failed to play sound file '${soundFile.getName()}'")
        }
    }

    override fun getVolume(): Float {
        return volume
    }

    override fun setVolume(volume: Float) {
        this.volume = volume
    }

    companion object {
        private const val AMBIENT_LUFS = -36
        private const val MUSIC_LUFS = -26
    }
}
