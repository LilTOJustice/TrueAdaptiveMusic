package liltojustice.trueadaptivemusic.client.sound.instance

import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.client.sound.SoundInstance
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvent

class VolumeControlledPositionedSoundInstance(sound: SoundEvent)
    : VolumeControlled,
    PositionedSoundInstance(
        sound.id,
        SoundCategory.MUSIC,
        1.0f,
        1.0f,
        SoundInstance.createRandom(),
        false,
        0,
        SoundInstance.AttenuationType.NONE,
        0.0,
        0.0,
        0.0,
        true
    ) {

    override fun getVolume(): Float {
        return volume
    }

    override fun setVolume(volume: Float) {
        this.volume = volume
    }
}