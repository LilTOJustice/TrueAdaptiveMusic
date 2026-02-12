package liltojustice.trueadaptivemusic.client.sound.instance

import net.minecraft.client.sound.AudioStream
import net.minecraft.client.sound.Sound
import net.minecraft.client.sound.SoundInstance
import net.minecraft.client.sound.SoundManager
import net.minecraft.client.sound.WeightedSoundSet
import net.minecraft.sound.SoundCategory
import net.minecraft.util.Identifier

abstract class TAMSoundInstance(val isAmbient: Boolean): SoundInstance {
    var desiredVolume = 1F
    abstract fun getAudioStream(): AudioStream?
    override fun getId(): Identifier? {
        return null
    }

    override fun getSoundSet(soundManager: SoundManager?): WeightedSoundSet? {
        return null
    }

    override fun getSound(): Sound {
        return Sound(
            Identifier.of("trueadaptivemusic", "file"),
            { 1F },
            { 1F },
            0,
            Sound.RegistrationType.FILE,
            true,
            false,
            0)
    }

    override fun getCategory(): SoundCategory? {
        return null
    }

    override fun isRepeatable(): Boolean {
        return false
    }

    override fun isRelative(): Boolean {
        return false
    }

    override fun getRepeatDelay(): Int {
        return 0
    }

    override fun getVolume(): Float {
        return 1F
    }

    override fun getPitch(): Float {
        return 1F
    }

    override fun getX(): Double {
        return 0.0
    }

    override fun getY(): Double {
        return 0.0
    }

    override fun getZ(): Double {
        return 0.0
    }

    override fun getAttenuationType(): SoundInstance.AttenuationType? {
        return null
    }
}