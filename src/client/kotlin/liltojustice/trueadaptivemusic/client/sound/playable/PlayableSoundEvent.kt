package liltojustice.trueadaptivemusic.client.sound.playable

import liltojustice.trueadaptivemusic.client.sound.instance.TAMSoundInstance
import liltojustice.trueadaptivemusic.client.sound.instance.SoundEventSoundInstance
import net.minecraft.resources.Identifier

class PlayableSoundEvent(private val identifier: Identifier): PlayableSound {
    override fun makeSoundInstance(isAmbient: Boolean, isLooping: Boolean, loopStartPoint: UInt): TAMSoundInstance {
        return SoundEventSoundInstance(this, identifier, isAmbient, isLooping)
    }

    override fun getSoundName(): String {
        return identifier.toString()
    }

    fun getId(): Identifier {
        return identifier
    }
}