package liltojustice.trueadaptivemusic.client.sound

import net.minecraft.client.sound.SoundInstance

interface PlayableSound {
    fun makeSoundInstance(): SoundInstance
    fun getSoundName(): String
}