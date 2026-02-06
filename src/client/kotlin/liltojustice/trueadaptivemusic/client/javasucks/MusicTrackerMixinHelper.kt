package liltojustice.trueadaptivemusic.client.javasucks

import liltojustice.trueadaptivemusic.client.javasucks.extensions.shouldIgnore
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.sound.MusicSound

object MusicTrackerMixinHelper {
    @JvmStatic
    fun shouldIgnore(sound: MusicSound): Boolean {
        return PositionedSoundInstance.music(sound.sound.value()).shouldIgnore()
    }
}