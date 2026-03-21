package liltojustice.trueadaptivemusic.client.sound.instance

import liltojustice.trueadaptivemusic.client.sound.playable.PlayableSound
import net.minecraft.client.MinecraftClient
import net.minecraft.client.sound.AudioStream
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.client.sound.Sound
import net.minecraft.client.sound.SoundManager
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvent
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.random.Random

class SoundEventSoundInstance(
    playableSound: PlayableSound,
    identifier: Identifier,
    isAmbient: Boolean,
    isLooping: Boolean
): TAMSoundInstance(playableSound, isAmbient, isLooping, 0U) {
    private val soundManager: SoundManager = MinecraftClient.getInstance().soundManager
    private val instance = PositionedSoundInstance(
        SoundEvent.of(identifier),
        SoundCategory.MUSIC,
        1F,
        1F,
        random,
        0.0,
        0.0,
        0.0)
    private var sound: Sound? = null

    init {
        instance.getSoundSet(soundManager)?.getSound(random)
        sound = instance.sound?.takeIf { it != SoundManager.MISSING_SOUND }
    }

    override fun getAudioStream(): AudioStream? {
        val sound = sound ?: return null
        val inputStreamGetter = { soundManager.soundSystem.soundLoader.resourceFactory.open(sound.location) }

        return getAudioStream(sound.location.toString(), "ogg", inputStreamGetter, isAmbient)
    }

    override fun getSoundString(): String {
        return sound?.identifier?.let { id ->
            Text.translatable(id.toShortTranslationKey().replace("/", ".")).string
        } ?: "Missing Sound"
    }

    override fun getSound(): Sound? {
        return sound
    }

    companion object {
        val random: Random = Random.create()
    }
}