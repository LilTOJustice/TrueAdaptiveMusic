package liltojustice.trueadaptivemusic.client.sound

import liltojustice.trueadaptivemusic.client.instance.AdaptiveMusicSoundInstance
import net.minecraft.client.sound.SoundInstance

class PlayableSoundFile(private val file: SoundFile): PlayableSound {
    override fun makeSoundInstance(): SoundInstance {
        return AdaptiveMusicSoundInstance(file)
    }

    override fun getSoundName(): String {
        return file.getName()
    }
}