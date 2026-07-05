package liltojustice.trueadaptivemusic.common.client.sound.playable

import liltojustice.trueadaptivemusic.common.client.sound.SoundLibrary
import liltojustice.trueadaptivemusic.common.client.sound.instance.TAMSoundInstance
import net.minecraft.resources.Identifier

interface PlayableSound {
    fun makeSoundInstance(
        isAmbient: Boolean = false, isLooping: Boolean = false, loopStartPoint: UInt = 0U): TAMSoundInstance?
    fun getSoundName(): String

    companion object {
        fun of(path: String, soundLibrary: SoundLibrary): PlayableSound? {
            return soundLibrary[path] ?: Identifier.tryParse(path)?.let { PlayableSoundEvent(it) }
        }
    }
}