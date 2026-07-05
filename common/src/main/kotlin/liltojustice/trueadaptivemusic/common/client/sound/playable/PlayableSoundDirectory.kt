package liltojustice.trueadaptivemusic.common.client.sound.playable

import liltojustice.trueadaptivemusic.common.client.sound.SoundLibrary
import liltojustice.trueadaptivemusic.common.client.sound.file.SoundFile
import liltojustice.trueadaptivemusic.common.client.sound.instance.AudioFileSoundInstance
import liltojustice.trueadaptivemusic.common.client.sound.instance.TAMSoundInstance

class PlayableSoundDirectory(private val directoryName: String, private val files: List<SoundFile>): PlayableSound {
    override fun makeSoundInstance(
        isAmbient: Boolean, isLooping: Boolean, loopStartPoint: UInt): TAMSoundInstance? {
        return files.takeIf { it.isNotEmpty() }?.let {
            AudioFileSoundInstance(this, it.random(), isAmbient, isLooping, loopStartPoint)
        }
    }

    override fun getSoundName(): String {
        return directoryName
    }

    fun getInteriorSounds(soundLibrary: SoundLibrary): List<PlayableSound> {
        return files.mapNotNull { PlayableSound.of(it.getName(), soundLibrary) }
    }
}