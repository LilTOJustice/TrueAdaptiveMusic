package liltojustice.trueadaptivemusic.client.sound.playable

import liltojustice.trueadaptivemusic.client.sound.instance.TAMSoundInstance
import liltojustice.trueadaptivemusic.client.sound.instance.SoundEventSoundInstance
import net.minecraft.resources.ResourceLocation

class PlayableSoundEvent(private val identifier: ResourceLocation): PlayableSound {
    override fun makeSoundInstance(isAmbient: Boolean, isLooping: Boolean, loopStartPoint: UInt): TAMSoundInstance {
        return SoundEventSoundInstance(this, identifier, isAmbient, isLooping)
    }

    override fun getSoundName(): String {
        return identifier.toString()
    }

    fun getId(): ResourceLocation {
        return identifier
    }
}