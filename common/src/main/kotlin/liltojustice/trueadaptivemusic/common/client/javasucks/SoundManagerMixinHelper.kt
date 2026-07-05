package liltojustice.trueadaptivemusic.common.client.javasucks

import liltojustice.trueadaptivemusic.common.client.javasucks.extensions.shouldIgnore
import net.minecraft.client.resources.sounds.SoundInstance

object SoundManagerMixinHelper {
    @JvmStatic
    fun shouldIgnore(sound: SoundInstance): Boolean {
        return sound.shouldIgnore()
    }
}