package liltojustice.trueadaptivemusic.client.sound.playable

import liltojustice.trueadaptivemusic.client.sound.file.SoundFile
import liltojustice.trueadaptivemusic.client.sound.instance.AudioFileSoundInstance
import liltojustice.trueadaptivemusic.client.sound.instance.TAMSoundInstance

class PlayableSoundDirectory(private val directoryName: String, private val files: List<SoundFile>): PlayableSound {
    override fun makeSoundInstance(isAmbient: Boolean): TAMSoundInstance {
        return AudioFileSoundInstance(files.random(), isAmbient)
    }

    override fun getSoundName(): String {
        return directoryName
    }
}