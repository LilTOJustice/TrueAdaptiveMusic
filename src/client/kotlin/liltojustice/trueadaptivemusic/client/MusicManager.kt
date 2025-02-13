package liltojustice.trueadaptivemusic.client

import liltojustice.trueadaptivemusic.client.instance.FadeInstance
import liltojustice.trueadaptivemusic.client.predicate.MusicPredicateTree
import liltojustice.trueadaptivemusic.client.sound.PlayableSound
import net.minecraft.client.MinecraftClient
import net.minecraft.client.option.SimpleOption
import net.minecraft.client.sound.SoundInstance
import net.minecraft.sound.SoundCategory

class MusicManager(
    private val client: MinecraftClient) {
    private var musicPack: MusicPack? = null
    private var currentMusicPredId: String = ""
    private var soundInstance: SoundInstance? = null
    private var oldSoundInstance: SoundInstance? = null
    private var toStop: SoundInstance? = null
    private var musicVolumeOption: SimpleOption<Double> = client.options.getSoundVolumeOption(SoundCategory.MUSIC)
    private val fadeInstances: MutableList<FadeInstance> = mutableListOf()

    init {
        client.soundManager.registerListener { instance, _ ->
            if (musicPack != null
                && instance.category == SoundCategory.MUSIC
                && instance != soundInstance
                && instance != oldSoundInstance) {
                toStop = instance
                setInstanceVolume(toStop!!, 0F)
            }
        }
    }

    fun selectMusicPack(musicPack: MusicPack) {
        stop()
        this.musicPack = musicPack
    }

    fun tick() {
        if (toStop != null) {
            client.soundManager.stop(toStop)
        }

        processFades()

        val predicateResult: MusicPredicateTree.Result? = musicPack?.rules?.getMusicToPlay(client)
        val nextMusic = predicateResult?.playableSounds?.ifEmpty { listOf(null) }?.random()
        val identifier = predicateResult?.path ?: ""
        if (!shouldPlay(nextMusic, identifier))
        {
            return
        }

        currentMusicPredId = identifier
        startNewMusic(nextMusic)
    }

    private fun processFades() {
        fadeInstances.forEach { fadeInstance ->
            val volume: Float = fadeInstance.tick()
            setInstanceVolume(fadeInstance.soundInstance, musicVolumeOption.value.toFloat() * volume)
        }

        fadeInstances.removeIf { fadeinstance -> fadeinstance.done() }
    }

    private fun shouldPlay(music: PlayableSound?, identifier: String): Boolean {
        return(music == null || identifier != currentMusicPredId || !isPlaying(soundInstance))
                && musicVolumeOption.value > 0
    }

    private fun startNewMusic(newMusic: PlayableSound?) {
        if (newMusic == null)
        {
            if (isPlaying(soundInstance)) {
                fadeInstances.add(FadeInstance(soundInstance!!, false))
                soundInstance = null
            }

            return
        }

        if (soundInstance == null) {
            soundInstance = newMusic.makeSoundInstance()
            client.soundManager.play(soundInstance)
            return
        }

        beginCrossfade(newMusic.makeSoundInstance())
    }

    private fun stop() {
        if (soundInstance != null)
        {
            client.soundManager.stop(soundInstance)
            client.soundManager.stop(oldSoundInstance)
            fadeInstances.forEach { fadeInstance -> client.soundManager.stop(fadeInstance.soundInstance) }
            fadeInstances.clear()
            soundInstance = null
            oldSoundInstance = null
        }
    }

    private fun beginCrossfade(newSoundInstance: SoundInstance) {
        oldSoundInstance = soundInstance
        soundInstance = newSoundInstance
        client.soundManager.play(soundInstance)

        fadeInstances.add(FadeInstance(soundInstance!!, true))
        fadeInstances.add(FadeInstance(oldSoundInstance!!, false))
    }

    private fun isPlaying(soundInstance: SoundInstance?): Boolean {
        return client.soundManager.isPlaying(soundInstance) &&
                !(client.soundManager.soundSystem.sources[soundInstance]?.isStopped ?: true)
    }

    private fun setInstanceVolume(soundInstance: SoundInstance, volume: Float) {
        client.soundManager.soundSystem.sources[soundInstance]?.run { source ->
            source.isPlaying
            source.setVolume(volume)
        }
    }
}