package liltojustice.trueadaptivemusic.client.sound.playable

import liltojustice.trueadaptivemusic.client.sound.instance.TAMSoundInstance

interface PlayableSound {
    fun makeSoundInstance(isAmbient: Boolean = false): TAMSoundInstance
    fun getSoundName(): String
}