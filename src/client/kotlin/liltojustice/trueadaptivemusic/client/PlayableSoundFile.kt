package liltojustice.trueadaptivemusic.client

import net.minecraft.client.sound.SoundInstance
import java.nio.file.Path

class PlayableSoundFile(private val fullPath: Path, predicateIdentifier: String): PlayableSound(predicateIdentifier) {
    override fun makeSoundInstance(): SoundInstance {
        return AdaptiveMusicSoundInstance(fullPath)
    }
}