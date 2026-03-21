package liltojustice.trueadaptivemusic.client.sound.instance

import liltojustice.trueadaptivemusic.client.sound.file.SoundFile
import liltojustice.trueadaptivemusic.client.sound.playable.PlayableSound
import net.minecraft.client.sound.AudioStream

class AudioFileSoundInstance(
    playableSound: PlayableSound,
    private val soundFile: SoundFile,
    isAmbient: Boolean,
    isLooping: Boolean,
    loopStartPoint: UInt
): TAMSoundInstance(playableSound, isAmbient, isLooping, loopStartPoint) {
    val fileName
        get() = soundFile.getName().split('.').dropLast(1).joinToString(".")

    override fun getAudioStream(): AudioStream {
        return getAudioStream(
            soundFile.getName(),
            soundFile.getExtension(),
            { soundFile.getInputStream() },
            isAmbient
        )
    }
}