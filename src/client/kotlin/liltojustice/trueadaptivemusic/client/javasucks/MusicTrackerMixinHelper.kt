package liltojustice.trueadaptivemusic.client.javasucks

import liltojustice.trueadaptivemusic.client.javasucks.extensions.shouldIgnore
import net.minecraft.sounds.Music

object MusicTrackerMixinHelper {
    @JvmStatic
    fun shouldIgnore(sound: Music): Boolean {
        return PositionedSoundInstance.music(sound.sound.value()).shouldIgnore()
    }
}