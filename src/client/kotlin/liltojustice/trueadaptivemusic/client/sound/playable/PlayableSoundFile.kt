package liltojustice.trueadaptivemusic.client.sound.playable

import liltojustice.trueadaptivemusic.client.sound.file.SoundFile
import liltojustice.trueadaptivemusic.client.sound.instance.AudioFileSoundInstance
import net.minecraft.client.sound.SoundInstance

class PlayableSoundFile(private val file: SoundFile): PlayableSound {
    override fun makeSoundInstance(isAmbient: Boolean): SoundInstance {
        return AudioFileSoundInstance(file, isAmbient)
    }

    override fun getSoundName(): String {
        return file.getName()
    }
}