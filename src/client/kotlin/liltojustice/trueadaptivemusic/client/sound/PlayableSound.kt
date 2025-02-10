package liltojustice.trueadaptivemusic.client.sound

import net.minecraft.client.sound.SoundInstance

abstract class PlayableSound(private val predicateIdentifier: String) {
    abstract fun makeSoundInstance(): SoundInstance
    abstract fun getSoundName(): String
    fun getPredicateIdentifier(): String { return predicateIdentifier }
}