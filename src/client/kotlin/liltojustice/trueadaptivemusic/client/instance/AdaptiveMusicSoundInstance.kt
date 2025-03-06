package liltojustice.trueadaptivemusic.client.instance

import liltojustice.trueadaptivemusic.Constants
import net.minecraft.client.sound.*
import net.minecraft.sound.SoundCategory
import java.io.InputStream

class AdaptiveMusicSoundInstance(private val stream: InputStream)
    : AbstractSoundInstance(Constants.TRUEADAPTIVEMUSIC_ID, SoundCategory.MUSIC) {
    init {
        sound = Sound(
            Constants.TRUEADAPTIVEMUSIC_ID.toString(),
            1.0F,
            1.0F,
            0,
            Sound.RegistrationType.FILE,
            true,
            false,
            0)
    }

    fun getAudioStream(): AudioStream {
        return OggAudioStream(stream)
    }
}
