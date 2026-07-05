package liltojustice.trueadaptivemusic.common.client.sound.playable

import liltojustice.trueadaptivemusic.common.client.sound.file.SoundFile
import liltojustice.trueadaptivemusic.common.client.sound.instance.AudioFileSoundInstance
import liltojustice.trueadaptivemusic.common.client.sound.instance.TAMSoundInstance

class PlayableSoundFile(private val file: SoundFile): PlayableSound {
    override fun makeSoundInstance(
        isAmbient: Boolean, isLooping: Boolean, loopStartPoint: UInt): TAMSoundInstance {
        return AudioFileSoundInstance(this, file, isAmbient, isLooping, loopStartPoint)
    }

    override fun getSoundName(): String {
        return file.getName()
    }
}