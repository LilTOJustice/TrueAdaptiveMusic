package liltojustice.trueadaptivemusic.client.sound

import liltojustice.trueadaptivemusic.client.instance.AdaptiveMusicSoundInstance
import net.minecraft.client.sound.SoundInstance
import kotlin.io.path.Path

class PlayableSoundFile(private val file: SoundFile): PlayableSound {
    override fun makeSoundInstance(): SoundInstance {
        return AdaptiveMusicSoundInstance(Path(file.getName()))
    }

    override fun getSoundName(): String {
        return file.getName()
    }
}