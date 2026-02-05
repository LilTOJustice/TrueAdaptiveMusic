package liltojustice.trueadaptivemusic.client.sound.instance

import liltojustice.trueadaptivemusic.Constants
import liltojustice.trueadaptivemusic.client.TAMClient
import liltojustice.trueadaptivemusic.client.music.pack.MusicLoadException
import liltojustice.trueadaptivemusic.client.sound.FFmpeg
import liltojustice.trueadaptivemusic.client.sound.file.SoundFile
import liltojustice.trueadaptivemusic.client.sound.stream.TruncatedAudioStream
import net.minecraft.client.sound.*
import net.minecraft.sound.SoundCategory
import net.minecraft.util.Identifier
import java.util.concurrent.CompletableFuture

class AudioFileSoundInstance(private val soundFile: SoundFile)
    : AbstractSoundInstance(
    Constants.AUDIO_FILE_STREAM_ID, SoundCategory.MUSIC, SoundInstance.createRandom()),
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
                    TruncatedAudioStream(FFmpeg.makeStream(soundFile)) }
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
}
