package liltojustice.trueadaptivemusic.client

import net.minecraft.client.sound.SoundInstance

abstract class PlayableSound(private val predicateIdentifier: String) {
    abstract fun makeSoundInstance(): SoundInstance
    fun getPredicateIdentifier(): String { return predicateIdentifier }
}