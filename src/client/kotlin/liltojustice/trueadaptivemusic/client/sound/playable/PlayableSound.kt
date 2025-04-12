package liltojustice.trueadaptivemusic.client.sound.playable

import net.minecraft.client.sound.SoundInstance

interface PlayableSound {
    fun makeSoundInstance(): SoundInstance
    fun getSoundName(): String
}