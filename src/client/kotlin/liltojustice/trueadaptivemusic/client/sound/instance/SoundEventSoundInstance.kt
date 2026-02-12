package liltojustice.trueadaptivemusic.client.sound.instance

import net.minecraft.client.MinecraftClient
import net.minecraft.client.sound.AudioStream
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.client.sound.Sound
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvent
import net.minecraft.util.Identifier
import net.minecraft.util.math.random.Random

class SoundEventSoundInstance(identifier: Identifier, isAmbient: Boolean) : TAMSoundInstance(isAmbient) {
    private val instance = PositionedSoundInstance(
        SoundEvent.of(identifier),
        SoundCategory.MUSIC,
        1F,
        1F,
        random,
        0.0,
        0.0,
        0.0)
    override fun getAudioStream(): AudioStream? {
        val soundManager = MinecraftClient.getInstance().soundManager
        instance.getSoundSet(soundManager)?.getSound(random)
        return instance.sound?.let {
            soundManager.soundSystem.soundLoader
                .loadStreamed(instance.sound?.location, false)
                .join()
        }
    }

    override fun getSound(): Sound? {
        return instance.sound
    }

    companion object {
        val random = Random.create()
    }
}