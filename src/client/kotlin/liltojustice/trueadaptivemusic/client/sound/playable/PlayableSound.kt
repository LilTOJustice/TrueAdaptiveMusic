package liltojustice.trueadaptivemusic.client.sound.playable

import net.minecraft.client.sound.SoundInstance

interface PlayableSound {
    fun makeSoundInstance(isAmbient: Boolean = false): SoundInstance
    fun getSoundName(): String
}