package liltojustice.trueadaptivemusic.client.sound.instance

import liltojustice.trueadaptivemusic.client.sound.file.SoundFile
import net.minecraft.client.sound.AudioStream

class AudioFileSoundInstance(private val soundFile: SoundFile, isAmbient: Boolean): TAMSoundInstance(isAmbient) {
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