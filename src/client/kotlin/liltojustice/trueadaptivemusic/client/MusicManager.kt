package liltojustice.trueadaptivemusic.client

import liltojustice.trueadaptivemusic.LogLevel
import liltojustice.trueadaptivemusic.Logger
import liltojustice.trueadaptivemusic.client.event.types.MusicEvent
import liltojustice.trueadaptivemusic.client.predicate.MusicPredicateTree
import liltojustice.trueadaptivemusic.client.sound.FadeManager
import liltojustice.trueadaptivemusic.client.sound.playable.PlayableSound
import liltojustice.trueadaptivemusic.client.sound.resumeInstance
import liltojustice.trueadaptivemusic.client.sound.setInstanceVolume
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
    private val fadeManager = FadeManager(client.soundManager, musicVolumeOption)
    private var onDemandSound: PlayableSound? = null
    private var onDemandSoundInstance: SoundInstance? = null
    private var timedIdentifier = ""
    private var timedIdentifierTimer = Timer()
    private var timedIdentifierTimerTask: TimerTask? = null
    private var shouldResume = false
    private var activeEvents: List<MusicEvent> = emptyList()

    init {
        InvokeMusicEventCallback.EVENT.register { eventType, args ->
            activeEvents.firstOrNull { event -> eventType == event.getTypeName() && event.validate(*args) }
                ?.let { event ->
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

    fun playNow(sound: PlayableSound?, keepBackground: Boolean = false) {
        if (sound == onDemandSound) {
            return
        }

        val targetVolume = if (keepBackground) BACKGROUND_VOLUME else 0F

        if (sound == null) {
            onDemandSoundInstance?.let {
                fadeManager.startFade(it, PLAY_NOW_FADE_TICKS, 0F, true)
            }
            onDemandSound = null
            onDemandSoundInstance = null
            currentSoundInstance?.let {
                fadeManager.startFade(it, PLAY_NOW_FADE_TICKS, 1F)
            }

            return
        }

        client.soundManager.stop(oldSoundInstance)
        currentSoundInstance?.let {
            fadeManager.startFade(it, PLAY_NOW_FADE_TICKS, targetVolume)
        }

        client.soundManager.stop(onDemandSoundInstance)
        onDemandSound = sound
        onDemandSound?.let {
            onDemandSoundInstance = it.makeSoundInstance()
            playInstance(onDemandSoundInstance)
        }
    }

    fun tick() {
        fadeManager.tick()

        if (onDemandSound != null) {
            if (!client.soundManager.isPlaying(onDemandSoundInstance)) {
                onDemandSound = null
                onDemandSoundInstance = null
                currentSoundInstance?.let {
                    fadeManager.startFade(it, PLAY_NOW_FADE_TICKS, 1F)
                }
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

    fun hasActiveEvent(eventName: String): Boolean {
        return activeEvents.any { event -> event.getTypeName() == eventName }
    }

    private fun shouldPlay(music: PlayableSound?, identifier: String): Boolean {
        return (music == null || identifier != currentMusicPredicateId || !isPlaying(currentSoundInstance))
                && musicVolumeOption.value > 0
    }

    private fun startNewMusic(newMusic: PlayableSound?) {
        if (newMusic == null)
        {
            if (isPlaying(currentSoundInstance)) {
                fadeManager.startFade(currentSoundInstance!!, REGULAR_FADE_TICKS, 0F)
                currentSoundInstance = null
            }

            return
        }

        if (currentSoundInstance == null) { //|| (!shouldResume && !isPlaying(oldSoundInstance))) {
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
        fadeManager.clearFades()
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
        if (oldSoundInstance != newSoundInstance) {
            oldSoundInstance?.let { fadeManager.startFade(it, REGULAR_FADE_TICKS, 0F, true) }
        }

        oldSoundInstance = currentSoundInstance
        currentSoundInstance = newSoundInstance

        if (shouldResume) {
            client.soundManager.resumeInstance(currentSoundInstance)
        }
        else {
            playInstance(currentSoundInstance)
        }

        client.soundManager.setInstanceVolume(currentSoundInstance, 0F, musicVolumeOption)
        fadeManager.startFade(currentSoundInstance!!, REGULAR_FADE_TICKS, 1F)
        fadeManager.startFade(oldSoundInstance!!, REGULAR_FADE_TICKS, 0F)
    }

    private fun isPlaying(soundInstance: SoundInstance?): Boolean {
        return client.soundManager.isPlaying(soundInstance) &&
                !(client.soundManager.soundSystem.sources[soundInstance]?.isStopped ?: true)
    }

    companion object {
        private const val REGULAR_FADE_TICKS = 50
        private const val PLAY_NOW_FADE_TICKS = 10
        private const val BACKGROUND_VOLUME = 0.2F
    }
}