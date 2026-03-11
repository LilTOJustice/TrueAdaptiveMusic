package liltojustice.trueadaptivemusic.client.sound.instance

import liltojustice.trueadaptivemusic.client.sound.file.SoundFile
import net.minecraft.client.sound.AudioStream

class AudioFileSoundInstance(
    private val soundFile: SoundFile,
    isAmbient: Boolean,
    isLooping: Boolean,
    loopStartPoint: UInt
): TAMSoundInstance(isAmbient, isLooping, loopStartPoint) {
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