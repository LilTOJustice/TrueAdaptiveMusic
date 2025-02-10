package liltojustice.trueadaptivemusic.client.sound

import liltojustice.trueadaptivemusic.client.instance.AdaptiveMusicSoundInstance
import net.minecraft.client.sound.SoundInstance
import java.nio.file.Path
import kotlin.io.path.name

class PlayableSoundFile(private val fullPath: Path, predicateIdentifier: String): PlayableSound(predicateIdentifier) {
    override fun makeSoundInstance(): SoundInstance {
        return AdaptiveMusicSoundInstance(fullPath)
    }

    override fun getSoundName(): String {
        return fullPath.name
    }
}