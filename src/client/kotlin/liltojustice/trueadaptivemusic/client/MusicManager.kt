package liltojustice.trueadaptivemusic.client

import liltojustice.trueadaptivemusic.LogLevel
import liltojustice.trueadaptivemusic.Logger
import liltojustice.trueadaptivemusic.client.instance.FadeInstance
import liltojustice.trueadaptivemusic.client.predicate.MusicPredicateTree
import liltojustice.trueadaptivemusic.client.sound.PlayableSound
import net.minecraft.client.MinecraftClient
import net.minecraft.client.option.SimpleOption
import net.minecraft.client.sound.SoundInstance
import net.minecraft.sound.SoundCategory
import java.util.*
import kotlin.concurrent.schedule
import kotlin.math.max

class MusicManager(
    private val client: MinecraftClient) {
    private var musicPack: MusicPack? = null
    private var currentMusicPredicateId: String = ""
    private var oldMusicPredicateId: String = ""
    private var currentSoundInstance: SoundInstance? = null
    private var oldSoundInstance: SoundInstance? = null
    private var toStop: SoundInstance? = null
    private var musicVolumeOption: SimpleOption<Double> = client.options.getSoundVolumeOption(SoundCategory.MUSIC)
    private val fadeInstances: MutableList<FadeInstance> = mutableListOf()
    private var onDemandSound: PlayableSound? = null
    private var onDemandSoundInstance: SoundInstance? = null
    private var timedIdentifier = ""
    private var timedIdentifierTimer = Timer()
    private var timedIdentifierTimerTask: TimerTask? = null
    private var shouldResume = false

    init {
        client.soundManager.registerListener { instance, _ ->
            if (musicPack != null
                && instance.category == SoundCategory.MUSIC
                && instance != currentSoundInstance
                && instance != oldSoundInstance
                && instance != onDemandSoundInstance) {
                toStop = instance
                setInstanceVolume(toStop!!, 0F)
            }
        }
    }

    fun selectMusicPack(musicPack: MusicPack?) {
        stop()
        this.musicPack = musicPack
    }

    fun getMusicPack(): MusicPack? {
        return musicPack
    }

    fun tick() {
        if (toStop != null) {
            client.soundManager.stop(toStop)
        }

        if (onDemandSound != null) {
            return
        }

        processFades()

        val predicateResult: MusicPredicateTree.Result? = musicPack?.rules?.getMusicToPlay(client)
        val identifier = predicateResult?.path ?: ""
        val parameters = predicateResult?.parameters ?: MusicPredicateTree.Node.Parameters()
        val trackDelayNoise = parameters.trackDelayNoise
        val trackDelay = parameters.trackDelay

        if (identifier == timedIdentifier) {
            return
        }
        else if (trackDelay != 0U && currentMusicPredicateId == identifier && !isPlaying(currentSoundInstance)) {
            val actualTrackDelay =
                max(0, (parameters.trackDelay.toInt() - trackDelayNoise.toInt()
                        ..parameters.trackDelay.toInt() + trackDelayNoise.toInt()).random()).toUInt()

            timedIdentifier = identifier
            timedIdentifierTimerTask = timedIdentifierTimer.schedule(actualTrackDelay.toLong() * 1000L) {
                timedIdentifier = ""
                currentMusicPredicateId = ""
            }

            return
        }
        else {
            timedIdentifierTimerTask?.cancel()
            timedIdentifier = ""
        }

        val nextMusic = predicateResult?.playableSounds?.ifEmpty { listOf(null) }?.random()

        if (!shouldPlay(nextMusic, identifier))
        {
            return
        }

        shouldResume = oldMusicPredicateId == identifier
        oldMusicPredicateId =
            if (identifier != currentMusicPredicateId)
                currentMusicPredicateId
            else
                oldMusicPredicateId
        currentMusicPredicateId = identifier
        startNewMusic(nextMusic)
    }

    fun playNow(sound: PlayableSound?) {
        if (sound == onDemandSound) {
            return
        }

        if (sound == null) {
            client.soundManager.stop(onDemandSoundInstance)
            onDemandSound = null
            onDemandSoundInstance = null

            return
        }

        client.soundManager.stopAll()
        onDemandSound = sound
        onDemandSound?.let {
            onDemandSoundInstance = it.makeSoundInstance()
            playInstance(onDemandSoundInstance)
        }
    }

    private fun processFades() {
        fadeInstances.forEach { fadeInstance ->
            val volume: Float = fadeInstance.tick()
            setInstanceVolume(fadeInstance.soundInstance, musicVolumeOption.value.toFloat() * volume)
        }

        fadeInstances.removeIf { fadeInstance -> fadeInstance.done() }
    }

    private fun shouldPlay(music: PlayableSound?, identifier: String): Boolean {
        return (music == null || identifier != currentMusicPredicateId || !isPlaying(currentSoundInstance))
                && musicVolumeOption.value > 0
    }

    private fun startNewMusic(newMusic: PlayableSound?) {
        if (newMusic == null)
        {
            if (isPlaying(currentSoundInstance)) {
                fadeInstances.add(FadeInstance(currentSoundInstance!!, false))
                currentSoundInstance = null
            }

            return
        }

        if (currentSoundInstance == null) {
            currentSoundInstance = newMusic.makeSoundInstance()
            playInstance(currentSoundInstance)
            if (!client.soundManager.isPlaying(currentSoundInstance)) {
                currentSoundInstance = null
                currentMusicPredicateId = ""
            }

            return
        }

        if (shouldResume) {
            oldSoundInstance?.let { beginCrossfade(it) }
        }
        else {
            beginCrossfade(newMusic.makeSoundInstance())
        }
    }

    private fun playInstance(soundInstance: SoundInstance?) {
        try {
            client.soundManager.play(soundInstance)
        }
        catch (e: MusicLoadException) {
            Logger.log("Error: Failed to play sound instance - ${e.message}", LogLevel.ERROR)
        }
    }

    private fun stop() {
        client.soundManager.stopAll()
        client.soundManager.close()
        currentSoundInstance = null
        oldSoundInstance = null
        onDemandSound = null
        onDemandSoundInstance = null
        timedIdentifierTimerTask?.cancel()
        timedIdentifier = ""
        timedIdentifierTimerTask = null
        currentMusicPredicateId = ""
        oldMusicPredicateId = ""
    }

    private fun beginCrossfade(newSoundInstance: SoundInstance) {
        oldSoundInstance = currentSoundInstance
        currentSoundInstance = newSoundInstance

        if (shouldResume) {
            client.soundManager.soundSystem.sources[currentSoundInstance]?.run { source -> source.resume() }
        }
        else {
            playInstance(currentSoundInstance)
        }

        fadeInstances.add(FadeInstance(currentSoundInstance!!, true))
        fadeInstances.add(FadeInstance(oldSoundInstance!!, false))
    }

    private fun isPlaying(soundInstance: SoundInstance?): Boolean {
        return client.soundManager.isPlaying(soundInstance) &&
                !(client.soundManager.soundSystem.sources[soundInstance]?.isStopped ?: true)
    }

    private fun setInstanceVolume(soundInstance: SoundInstance, volume: Float) {
        client.soundManager.soundSystem.sources[soundInstance]?.run { source ->
            source.setVolume(volume)
            if (volume != 0F) {
                return@run
            }

            if (soundInstance === oldSoundInstance) {
                source.pause()
            }
            else {
                source.stop()
            }
        }
    }
}