package liltojustice.trueadaptivemusic.client

import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.client.sound.SoundInstance
import net.minecraft.sound.SoundEvent

class PlayableSoundEvent(private val soundEvent: SoundEvent, predicateIdentifier: String): PlayableSound(predicateIdentifier) {
    override fun makeSoundInstance(): SoundInstance {
        return PositionedSoundInstance.music(soundEvent)
    }
}