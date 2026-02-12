package liltojustice.trueadaptivemusic.client.sound.playable

import liltojustice.trueadaptivemusic.client.sound.instance.TAMSoundInstance
import liltojustice.trueadaptivemusic.client.sound.instance.SoundEventSoundInstance
import net.minecraft.util.Identifier

class PlayableSoundEvent(private val identifier: Identifier): PlayableSound {
    override fun makeSoundInstance(isAmbient: Boolean): TAMSoundInstance {
        return SoundEventSoundInstance(identifier, isAmbient)
    }

    override fun getSoundName(): String {
        return identifier.path
    }
}