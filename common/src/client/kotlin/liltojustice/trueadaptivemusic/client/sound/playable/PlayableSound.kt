package liltojustice.trueadaptivemusic.client.sound.playable

import liltojustice.trueadaptivemusic.client.sound.SoundLibrary
import liltojustice.trueadaptivemusic.client.sound.instance.TAMSoundInstance
import net.minecraft.ResourceLocationException
import net.minecraft.resources.ResourceLocation

interface PlayableSound {
    fun makeSoundInstance(
        isAmbient: Boolean = false, isLooping: Boolean = false, loopStartPoint: UInt = 0U): TAMSoundInstance?
    fun getSoundName(): String

    companion object {
        fun of(path: String, soundLibrary: SoundLibrary): PlayableSound? {
            return soundLibrary[path] ?: try {
                PlayableSoundEvent(ResourceLocation.parse(path))
            }
            catch (_: ResourceLocationException) {
                null
            }
        }
    }
}