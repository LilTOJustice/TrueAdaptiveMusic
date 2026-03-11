package liltojustice.trueadaptivemusic.client.sound.playable

import liltojustice.trueadaptivemusic.client.sound.SoundLibrary
import liltojustice.trueadaptivemusic.client.sound.file.SoundFile
import liltojustice.trueadaptivemusic.client.sound.instance.AudioFileSoundInstance
import liltojustice.trueadaptivemusic.client.sound.instance.TAMSoundInstance

class PlayableSoundDirectory(private val directoryName: String, private val files: List<SoundFile>): PlayableSound {
    override fun makeSoundInstance(isAmbient: Boolean, isLooping: Boolean, loopStartPoint: UInt): TAMSoundInstance {
        return AudioFileSoundInstance(files.random(), isAmbient, isLooping, loopStartPoint)
    }

    override fun getSoundName(): String {
        return directoryName
    }

    fun getInteriorSounds(soundLibrary: SoundLibrary): List<PlayableSound> {
        return files.mapNotNull { PlayableSound.of(it.getName(), soundLibrary) }
    }
}