package liltojustice.trueadaptivemusic.client.instance

import liltojustice.trueadaptivemusic.Constants
import net.minecraft.client.sound.*
import net.minecraft.sound.SoundCategory
import java.nio.file.Path

class AdaptiveMusicSoundInstance(path: Path)
    : AbstractSoundInstance(Constants.TRUEADAPTIVEMUSIC_ID, SoundCategory.MUSIC) {
        init {
            sound = Sound(
                Constants.TRUEADAPTIVEMUSIC_ID.toString(),
                1.0F,
                1.0F,
                0,
                Sound.RegistrationType.SOUND_EVENT,
                true,
                false,
                0)
        }
}
