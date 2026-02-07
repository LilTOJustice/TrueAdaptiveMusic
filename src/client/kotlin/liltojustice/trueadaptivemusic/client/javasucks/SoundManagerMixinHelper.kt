package liltojustice.trueadaptivemusic.client.javasucks

import liltojustice.trueadaptivemusic.client.javasucks.extensions.shouldIgnore
import net.minecraft.client.sound.SoundInstance

object SoundManagerMixinHelper {
    @JvmStatic
    fun shouldIgnore(sound: SoundInstance): Boolean {
        return sound.shouldIgnore()
    }
}