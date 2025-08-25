package liltojustice.trueadaptivemusic.client.music

import liltojustice.trueadaptivemusic.Logger
import liltojustice.trueadaptivemusic.client.InvokeMusicEventCallback
import liltojustice.trueadaptivemusic.client.TAMClient
import liltojustice.trueadaptivemusic.client.trigger.event.MusicEvent
import liltojustice.trueadaptivemusic.client.sound.VolumeManager
import liltojustice.trueadaptivemusic.client.sound.playable.PlayableSound
import liltojustice.trueadaptivemusic.client.sound.resumeInstance
import liltojustice.trueadaptivemusic.client.trigger.event.types.OnEnterPredicateEvent
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicateTree
import net.minecraft.client.MinecraftClient
import net.minecraft.client.option.SimpleOption
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.client.sound.SoundInstance
import net.minecraft.sound.SoundCategory
import net.minecraft.util.ActionResult
import net.minecraft.util.math.Vec3d
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
    private var musicVolumeOption: SimpleOption<Double> =
        client.options.getSoundVolumeOption(SoundCategory.MUSIC)
    private val volumeManager = VolumeManager(client.soundManager, musicVolumeOption)
    private var onDemandSound: PlayableSound? = null
    private var onDemandSoundInstance: SoundInstance? = null
    var playingEvent: MusicEvent? = null
        private set
    private var timedIdentifier = ""
    private var timedIdentifierTimer = Timer()
    private var timedIdentifierTimerTask: TimerTask? = null
    private var shouldResume = false
    private var activeEvents: List<MusicEvent> = emptyList()
    private var keepBackground = false
    private var pauseDone = false

    init {
        InvokeMusicEventCallback.EVENT.register { eventType, args ->
            activeEvents.firstOrNull { event ->
                eventType == event.getTypeName()
                        && runCatching { event.validate(*args) }.getOrNull() == true }
                ?.let { event ->
                    event.playableSounds.randomOrNull()?.let {
                        playNow(it, true)
                    }
                    playingEvent = event
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
        playingEvent = null
        this.keepBackground = keepBackground
        if (sound == onDemandSound) {
            return
        }

        val targetVolume = if (keepBackground) BACKGROUND_VOLUME else 0F

        if (sound == null) {
            onDemandSoundInstance?.let {
                volumeManager.startFade(
                    it, PLAY_NOW_FADE_TICKS, 0F, true)
            }
            onDemandSound = null
            onDemandSoundInstance = null
            currentSoundInstance?.let {
                volumeManager.startFade(it, PLAY_NOW_FADE_TICKS, 1F)
            }

            return
        }

        client.soundManager.stop(oldSoundInstance)
        currentSoundInstance?.let {
            volumeManager.startFade(it, PLAY_NOW_FADE_TICKS, targetVolume)
        }

        client.soundManager.stop(onDemandSoundInstance)
        onDemandSound = sound
        onDemandSound?.let {
            onDemandSoundInstance = it.makeSoundInstance()
            playInstance(onDemandSoundInstance)
        }
    }

    fun tick() {
        if (client.isPaused && !pauseDone) {
            currentSoundInstance
                ?.let {
                    volumeManager.startFade(
                        it, PAUSE_FADE_TICKS, PAUSE_VOLUME) }
            pauseDone = true
        }
        else if (!client.isPaused && pauseDone) {
            currentSoundInstance
                ?.let {
                    volumeManager.startFade(
                        it, PAUSE_FADE_TICKS, 1F) }

            pauseDone = false
        }

        volumeManager.tick()

        if (onDemandSound != null) {
            if (!client.soundManager.isPlaying(onDemandSoundInstance)) {
                onDemandSound = null
                onDemandSoundInstance = null
                playingEvent = null
                keepBackground = false
                currentSoundInstance?.let {
                    volumeManager.startFade(it, PLAY_NOW_FADE_TICKS, 1F)
                }
            }

            if (!keepBackground) {
                return
            }
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

        val nextMusic =
            if (jukeboxPlaying())
                null
            else
                predicateResult?.playableSounds?.ifEmpty { listOf(null) }?.random()

        if (!shouldPlay(nextMusic, identifier))
        {
            return
        }

        if (identifier != currentMusicPredicateId
            && predicateResult?.events?.any { event -> event is OnEnterPredicateEvent } ?: false) {
            MusicEvent.invokeMusicEvent(TAMClient.eventRegistry[OnEnterPredicateEvent::class])
        }

        shouldResume = oldMusicPredicateId == identifier
        oldMusicPredicateId =
            if (identifier != currentMusicPredicateId)
                currentMusicPredicateId
            else
                oldMusicPredicateId
        currentMusicPredicateId = identifier
        startNewMusic(nextMusic)

        if (pauseDone) {
            currentSoundInstance
                ?.let {
                    volumeManager.startFade(
                        it, PAUSE_FADE_TICKS, PAUSE_VOLUME) }
        }
    }

    fun hasSoundInstance(instance: SoundInstance): Boolean {
        return currentSoundInstance === instance || oldSoundInstance === instance || onDemandSoundInstance === instance
    }

    private fun shouldPlay(music: PlayableSound?, identifier: String): Boolean {
        return (music == null || identifier != currentMusicPredicateId || !isPlaying(currentSoundInstance))
                && musicVolumeOption.value > 0
    }

    private fun startNewMusic(newMusic: PlayableSound?) {
        if (newMusic == null)
        {
            if (isPlaying(currentSoundInstance)) {
                volumeManager.startFade(
                    currentSoundInstance!!,
                    REGULAR_FADE_TICKS,
                    0F,
                    true)
                currentSoundInstance = null
            }

            return
        }

        if (currentSoundInstance == null) {
            currentSoundInstance = newMusic.makeSoundInstance()
            playInstance(currentSoundInstance, keepBackground)
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

    private fun playInstance(soundInstance: SoundInstance?, background: Boolean = false) {
        try {
            client.soundManager.play(soundInstance)
            if (background) {
                soundInstance?.let { volumeManager.setInstanceVolume(it, BACKGROUND_VOLUME) }
            }
        }
        catch (e: MusicLoadException) {
            Logger.logError("Error: Failed to play sound instance - ${e.message}")
        }
    }

    private fun stop() {
        volumeManager.clearFades()
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
        activeEvents = emptyList()
    }

    private fun beginCrossfade(newSoundInstance: SoundInstance) {
        if (oldSoundInstance != newSoundInstance) {
            oldSoundInstance?.let {
                volumeManager.startFade(
                    it, REGULAR_FADE_TICKS, 0F, true) }
        }

        oldSoundInstance = currentSoundInstance
        currentSoundInstance = newSoundInstance

        if (shouldResume) {
            if (keepBackground) {
                volumeManager.setInstanceVolume(currentSoundInstance!!, BACKGROUND_VOLUME)
            }

            client.soundManager.resumeInstance(currentSoundInstance)
        }
        else {
            playInstance(currentSoundInstance, keepBackground)
        }

        if (!keepBackground) {
            volumeManager.setInstanceVolume(currentSoundInstance!!, 0F)
            volumeManager.startFade(
                currentSoundInstance!!, REGULAR_FADE_TICKS, 1F)
        }

        volumeManager.startFade(oldSoundInstance!!, REGULAR_FADE_TICKS, 0F)
    }

    private fun isPlaying(soundInstance: SoundInstance?): Boolean {
        return client.soundManager.isPlaying(soundInstance) &&
                !(client.soundManager.soundSystem.sources[soundInstance]?.isStopped ?: true)
    }

    private fun jukeboxPlaying(): Boolean {
        return client.soundManager.soundSystem.sources.keys.any {
            instance -> instance.category == SoundCategory.RECORDS
                && instance is PositionedSoundInstance
                && client.player?.let {
                    Vec3d(instance.x, instance.y, instance.z)
                        .squaredDistanceTo(it.pos) < instance.sound.attenuation * instance.sound.attenuation * 4
                } ?: false
        }
    }

    companion object {
        private const val REGULAR_FADE_TICKS = 50
        private const val PLAY_NOW_FADE_TICKS = 10
        private const val PAUSE_FADE_TICKS = 5
        private const val BACKGROUND_VOLUME = 0.2F
        private const val PAUSE_VOLUME = 0.2F
    }
}