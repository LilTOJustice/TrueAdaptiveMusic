package liltojustice.trueadaptivemusic.client.sound.playable

import liltojustice.trueadaptivemusic.client.sound.instance.VolumeControlledPositionedSoundInstance
import net.minecraft.client.sound.SoundInstance
import net.minecraft.sound.SoundEvent

class PlayableSoundEvent(private val soundEvent: SoundEvent): PlayableSound {
    override fun makeSoundInstance(isAmbient: Boolean): SoundInstance {
        return VolumeControlledPositionedSoundInstance(soundEvent, isAmbient)
    }

    override fun getSoundName(): String {
        return soundEvent.id.path
    }
}