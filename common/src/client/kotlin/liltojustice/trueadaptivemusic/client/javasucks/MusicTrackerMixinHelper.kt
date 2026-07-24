package liltojustice.trueadaptivemusic.client.javasucks

import liltojustice.trueadaptivemusic.client.javasucks.extensions.shouldIgnore
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.sounds.Music

object MusicTrackerMixinHelper {
    @JvmStatic
    fun shouldIgnore(sound: Music): Boolean {
        return SimpleSoundInstance.forMusic(sound.event.value()).shouldIgnore()
    }
}