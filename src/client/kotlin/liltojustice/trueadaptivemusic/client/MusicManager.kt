package liltojustice.trueadaptivemusic.client

import liltojustice.trueadaptivemusic.LogLevel
import liltojustice.trueadaptivemusic.Logger
import liltojustice.trueadaptivemusic.client.event.types.MusicEvent
import liltojustice.trueadaptivemusic.client.event.types.OnAdvancementGetEvent
import liltojustice.trueadaptivemusic.client.instance.FadeInstance
import liltojustice.trueadaptivemusic.client.predicate.MusicPredicateTree
import liltojustice.trueadaptivemusic.client.sound.PlayableSound
import net.minecraft.client.MinecraftClient
import net.minecraft.client.option.SimpleOption
import net.minecraft.client.sound.SoundInstance
import net.minecraft.sound.SoundCategory
import net.minecraft.util.ActionResult
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
    private var musicVolumeOption: SimpleOption<Double> = client.options.getSoundVolumeOption(SoundCategory.MUSIC)
    private val fadeInstances: MutableMap<SoundInstance, FadeInstance> = mutableMapOf()
    private var onDemandSound: PlayableSound? = null
    private var onDemandSoundInstance: SoundInstance? = null
    private var timedIdentifier = ""
    private var timedIdentifierTimer = Timer()
    private var timedIdentifierTimerTask: TimerTask? = null
    private var shouldResume = false
    private var activeEvents: List<MusicEvent> = emptyList()

    init {
        InvokeMusicEventCallback.EVENT.register { eventType ->
            activeEvents.filter { event -> eventType == event.getTypeName() }.forEach { event ->
                event.playableSounds.randomOrNull()?.let {
                    playNow(it, true)
                }
            }

            ActionResult.PASS
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
        processFades()

        if (onDemandSound != null) {
            if (!client.soundManager.isPlaying(onDemandSoundInstance)) {
                onDemandSound = null
                onDemandSoundInstance = null
                currentSoundInstance?.let {
                    resumeSound(it)
                    fadeInstances[it] = FadeInstance(it, true, PLAY_NOW_FADE_TICKS, 0.2F)
                }
                processFades()
            }

            return
        }

        val predicateResult: MusicPredicateTree.Result? = musicPack?.rules?.getMusicToPlay(client)
        val identifier = predicateResult?.path ?: ""
        val parameters = predicateResult?.parameters ?: MusicPredicateTree.Node.Parameters()
        val trackDelayNoise = parameters.trackDelayNoise
        val trackDelay = parameters.trackDelay
        activeEvents = predicateResult?.events ?: emptyList()

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

    fun playNow(sound: PlayableSound?, keepBackground: Boolean = false) {
        if (sound == onDemandSound) {
            return
        }

        if (sound == null) {
            client.soundManager.stop(onDemandSoundInstance)
            onDemandSound = null
            onDemandSoundInstance = null
            currentSoundInstance?.let {
                resumeSound(it)
                fadeInstances[it] = FadeInstance(it, true, PLAY_NOW_FADE_TICKS, if (keepBackground) 0.2F else 0F)
            }

            return
        }

        client.soundManager.stop(oldSoundInstance)
        currentSoundInstance?.let {
            fadeInstances[it] = FadeInstance(it, false, PLAY_NOW_FADE_TICKS, if (keepBackground) 0.2F else 0F)
        }

        client.soundManager.stop(onDemandSoundInstance)
        onDemandSound = sound
        onDemandSound?.let {
            onDemandSoundInstance = it.makeSoundInstance()
            playInstance(onDemandSoundInstance)
        }
    }

    fun hasAdvancementEvent(): Boolean {
        return activeEvents.any { event -> event.getTypeName() == OnAdvancementGetEvent.getTypeName() }
    }

    private fun processFades() {
        fadeInstances.values.forEach { fadeInstance ->
            val volume: Float = fadeInstance.tick()
            setInstanceVolume(fadeInstance.soundInstance, musicVolumeOption.value.toFloat() * volume)
        }

        fadeInstances.values.forEach { fadeInstance -> if (fadeInstance.done()) fadeInstances.remove(fadeInstance.soundInstance) }
    }

    private fun shouldPlay(music: PlayableSound?, identifier: String): Boolean {
        return (music == null || identifier != currentMusicPredicateId || !isPlaying(currentSoundInstance))
                && musicVolumeOption.value > 0
    }

    private fun startNewMusic(newMusic: PlayableSound?) {
        if (newMusic == null)
        {
            if (isPlaying(currentSoundInstance)) {
                fadeInstances[currentSoundInstance!!] = FadeInstance(currentSoundInstance!!, false)
                currentSoundInstance = null
            }

            return
        }

        if (currentSoundInstance == null || (!shouldResume && !isPlaying(oldSoundInstance))) {
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
            resumeSound(currentSoundInstance)
        }
        else {
            playInstance(currentSoundInstance)
        }

        fadeInstances[currentSoundInstance!!] = FadeInstance(currentSoundInstance!!, true)
        fadeInstances[oldSoundInstance!!] = FadeInstance(oldSoundInstance!!, false)
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

            if (soundInstance === oldSoundInstance || onDemandSoundInstance != null) {
                source.pause()
            }
            else {
                source.stop()
            }
        }
    }

    private fun resumeSound(sound: SoundInstance?) {
        sound?.let {
            client.soundManager.soundSystem.sources[it]?.run { source -> source.resume() }
        }
    }

    companion object {
        private const val PLAY_NOW_FADE_TICKS = 10
    }
}