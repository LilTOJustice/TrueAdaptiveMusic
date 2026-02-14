package liltojustice.trueadaptivemusic.client.sound.playable

import liltojustice.trueadaptivemusic.client.sound.file.SoundFile
import liltojustice.trueadaptivemusic.client.sound.instance.AudioFileSoundInstance
import liltojustice.trueadaptivemusic.client.sound.instance.TAMSoundInstance

class PlayableSoundFile(private val file: SoundFile): PlayableSound {
    override fun makeSoundInstance(isAmbient: Boolean): TAMSoundInstance {
        return AudioFileSoundInstance(file, isAmbient)
    }

    override fun getSoundName(): String {
        return file.getName()
    }
}