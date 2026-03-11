package liltojustice.trueadaptivemusic.client.sound.playable

import liltojustice.trueadaptivemusic.client.sound.instance.TAMSoundInstance
import liltojustice.trueadaptivemusic.client.sound.instance.SoundEventSoundInstance
import net.minecraft.util.Identifier

class PlayableSoundEvent(private val identifier: Identifier): PlayableSound {
    override fun makeSoundInstance(isAmbient: Boolean, isLooping: Boolean, loopStartPoint: UInt): TAMSoundInstance {
        return SoundEventSoundInstance(identifier, isAmbient, isLooping)
    }

    override fun getSoundName(): String {
        return identifier.toString()
    }
}