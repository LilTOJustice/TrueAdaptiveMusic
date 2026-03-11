package liltojustice.trueadaptivemusic.client.sound.playable

import liltojustice.trueadaptivemusic.client.sound.SoundLibrary
import liltojustice.trueadaptivemusic.client.sound.instance.TAMSoundInstance
import net.minecraft.util.Identifier
import net.minecraft.util.InvalidIdentifierException

interface PlayableSound {
    fun makeSoundInstance(
        isAmbient: Boolean = false, isLooping: Boolean = false, loopStartPoint: UInt = 0U): TAMSoundInstance
    fun getSoundName(): String

    companion object {
        fun of(path: String, soundLibrary: SoundLibrary): PlayableSound? {
            return soundLibrary[path] ?: try {
                PlayableSoundEvent(Identifier.of(path))
            }
            catch (_: InvalidIdentifierException) {
                null
            }
        }
    }
}