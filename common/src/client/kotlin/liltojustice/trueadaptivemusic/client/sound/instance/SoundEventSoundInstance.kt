package liltojustice.trueadaptivemusic.client.sound.instance

import liltojustice.trueadaptivemusic.client.sound.playable.PlayableSound
import net.minecraft.client.Minecraft
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.client.resources.sounds.Sound
import net.minecraft.client.sounds.AudioStream
import net.minecraft.client.sounds.SoundManager
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundSource
import net.minecraft.util.RandomSource

class SoundEventSoundInstance(
    playableSound: PlayableSound,
    identifier: Identifier,
    isAmbient: Boolean,
    isLooping: Boolean
): TAMSoundInstance(playableSound, isAmbient, isLooping, 0U) {
    private val soundManager: SoundManager = Minecraft.getInstance().soundManager
    private val instance = SimpleSoundInstance(
        SoundEvent.createVariableRangeEvent(identifier),
        SoundSource.MUSIC,
        1F,
        1F,
        random,
        0.0,
        0.0,
        0.0
    )
    private var sound: Sound? = null

    init {
        instance.resolve(soundManager)?.getSound(random)
        sound = instance.sound?.takeIf { it != SoundManager.EMPTY_SOUND }
    }

    override fun getAudioStream(): AudioStream? {
        val sound = sound ?: return null
        val inputStreamGetter = { soundManager.soundEngine.soundBuffers.resourceManager.open(sound.path) }

        return getAudioStream(sound.location.toString(), inputStreamGetter, isAmbient)
    }

    override fun getSoundString(): String {
        return sound?.location?.let { id ->
            Component.translatable(id.toShortLanguageKey().replace("/", ".")).string
        } ?: "Missing Sound"
    }

    override fun getSound(): Sound? {
        return sound
    }

    companion object {
        val random: RandomSource = RandomSource.create()
    }
}