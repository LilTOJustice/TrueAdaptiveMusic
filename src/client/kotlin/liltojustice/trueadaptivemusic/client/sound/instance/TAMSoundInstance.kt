package liltojustice.trueadaptivemusic.client.sound.instance

import liltojustice.trueadaptivemusic.client.TAMClient
import liltojustice.trueadaptivemusic.client.music.pack.MusicLoadException
import liltojustice.trueadaptivemusic.client.sound.FFmpeg
import liltojustice.trueadaptivemusic.client.sound.stream.FFmpegAudioStream
import liltojustice.trueadaptivemusic.client.sound.stream.TruncatedAudioStream
import net.minecraft.client.sound.AudioStream
import net.minecraft.client.sound.OggAudioStream
import net.minecraft.client.sound.Sound
import net.minecraft.client.sound.SoundInstance
import net.minecraft.client.sound.SoundManager
import net.minecraft.client.sound.WeightedSoundSet
import net.minecraft.sound.SoundCategory
import net.minecraft.util.Identifier
import java.io.InputStream

abstract class TAMSoundInstance(
    val isAmbient: Boolean, val isLooping: Boolean, val loopStartPoint: UInt): SoundInstance {
    var desiredVolume = 1F
    abstract fun getAudioStream(): AudioStream?
    override fun getId(): Identifier? {
        return null
    }

    override fun getSoundSet(soundManager: SoundManager?): WeightedSoundSet? {
        return null
    }

    override fun getSound(): Sound? {
        return Sound(
            Identifier.of("trueadaptivemusic", "file"),
            { 1F },
            { 1F },
            0,
            Sound.RegistrationType.FILE,
            true,
            false,
            0)
    }

    override fun getCategory(): SoundCategory? {
        return null
    }

    override fun isRepeatable(): Boolean {
        return false
    }

    override fun isRelative(): Boolean {
        return false
    }

    override fun getRepeatDelay(): Int {
        return 0
    }

    override fun getVolume(): Float {
        return 1F
    }

    override fun getPitch(): Float {
        return 1F
    }

    override fun getX(): Double {
        return 0.0
    }

    override fun getY(): Double {
        return 0.0
    }

    override fun getZ(): Double {
        return 0.0
    }

    override fun getAttenuationType(): SoundInstance.AttenuationType? {
        return null
    }

    companion object {
        private const val AMBIENT_LUFS = -36
        private const val MUSIC_LUFS = -26

        fun getAudioStream(
            name: String,
            extension: String,
            inputStreamGetter: () -> InputStream,
            isAmbient: Boolean
        ): AudioStream {
            try {
                return if (!TAMClient.hasFFmpeg && extension == "ogg") {
                    TruncatedAudioStream(OggAudioStream(inputStreamGetter()))
                }
                else {
                    val loudnessUnits = if (isAmbient)
                        AMBIENT_LUFS + TAMClient.options.ambienceLoudnessBoost.value.toInt()
                    else
                        MUSIC_LUFS + TAMClient.options.musicLoudnessBoost.value.toInt()
                    TruncatedAudioStream(
                        FFmpegAudioStream(
                            inputStreamGetter(),
                            FFmpeg.getFileAudioFormat(inputStreamGetter()),
                            loudnessUnits
                        )
                    )
                }
            }
            catch (e: Exception) {
                throw MusicLoadException("Failed to load audio stream for '$name'", e)
            }
        }
    }
}