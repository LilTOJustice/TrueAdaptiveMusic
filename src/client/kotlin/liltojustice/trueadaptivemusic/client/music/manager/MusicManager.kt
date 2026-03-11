package liltojustice.trueadaptivemusic.client.music.manager

import liltojustice.trueadaptivemusic.client.music.pack.MusicPackOptions
import liltojustice.trueadaptivemusic.client.music.tree.MusicTree
import liltojustice.trueadaptivemusic.client.trigger.event.MusicEvent
import liltojustice.trueadaptivemusic.client.sound.playable.PlayableSound
import liltojustice.trueadaptivemusic.client.trigger.event.types.OnEnterPredicateEvent
import net.minecraft.client.MinecraftClient
import net.minecraft.client.option.SimpleOption
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.sound.SoundCategory
import net.minecraft.util.math.Vec3d
import kotlin.math.max
import kotlin.reflect.KClass

class MusicManager(private val client: MinecraftClient) {
    var playingEvent: MusicEvent? = null
        private set

    private val musicPlayer = MusicPlayer(client)
    private var currentMusicPredicateId: String = ""
    private var oldMusicPredicateId: String = ""
    private var musicVolumeOption: SimpleOption<Double> =
        client.options.getSoundVolumeOption(SoundCategory.MUSIC)
    private var masterVolumeOption: SimpleOption<Double> =
        client.options.getSoundVolumeOption(SoundCategory.MASTER)
    private var eventPool: List<MusicEvent> = emptyList()
    private var lastMusic: PlayableSound? = null
    private var lastAmbience: PlayableSound? = null
    private var mainTrack = MAIN_TRACK_1
    private var ambienceTrack = AMBIENCE_TRACK_1

    init {
        musicPlayer.createTrack(MAIN_TRACK_1, false, MAIN_CROSSFADE_TICKS)
        musicPlayer.createTrack(MAIN_TRACK_2, false, MAIN_CROSSFADE_TICKS)
        musicPlayer.createTrack(AMBIENCE_TRACK_1, true, MAIN_CROSSFADE_TICKS)
        musicPlayer.createTrack(AMBIENCE_TRACK_2, true, MAIN_CROSSFADE_TICKS)
        musicPlayer.createTrack(EVENT_TRACK, false, ON_DEMAND_CROSSFADE_TICKS)
        musicPlayer.createTrack(ON_DEMAND_TRACK, false, ON_DEMAND_CROSSFADE_TICKS)
    }

    fun <T: MusicEvent> invokeMusicEvent(eventType: KClass<T>, vararg args: Any?) {
        eventPool.firstOrNull { event ->
            eventType == event::class && runCatching { event.validate(*args) }.getOrNull() == true }
            ?.let { event ->
                event.music.randomOrNull()?.let {
                    musicPlayer.startNew(EVENT_TRACK, it)
                }
                playingEvent = event
            }
    }

    fun refreshSoundVolume() {
        musicPlayer.refreshSoundVolume()
    }

    fun playNow(onDemandSound: PlayableSound?) {
        onDemandSound?.let { musicPlayer.startNew(ON_DEMAND_TRACK, onDemandSound) }
            ?: musicPlayer.stop(ON_DEMAND_TRACK)
    }

    fun stop() {
        client.musicTracker.setCurrent(null)
        musicPlayer.stopAll()
        currentMusicPredicateId = ""
        oldMusicPredicateId = ""
        eventPool = emptyList()
        lastMusic = null
    }

    fun tick(treeResult: MusicTree.Result, packOptions: MusicPackOptions) {
        if (masterVolumeOption.value == 0.0) {
            return
        }

        val identifier = treeResult.path
        val parameters = treeResult.parameters
        val musicToPlay = treeResult.accumulatedMusic
        val ambienceToPlay = treeResult.accumulatedAmbience
        val trackDelayNoise = parameters.trackDelayNoise
        val trackDelay = parameters.trackDelay
        val enterDelay = parameters.enterDelay
        val loopMusic = parameters.loopMusic
        val loopStartPoints = parameters.loopStartPoints
        val shouldResume = oldMusicPredicateId == identifier && enterDelay == 0U
        val isEnter = currentMusicPredicateId != identifier
        val persistentNodeMusic = packOptions.persistentNodeMusic && !loopMusic

        eventPool = treeResult.accumulatedEvents

        val isPaused = isPaused(client)
        val shouldStop = shouldStopMain(client, musicPlayer, musicToPlay)

        musicPlayer.clampTrackVolume(
            EVENT_TRACK,
            if (isPaused) {
                PAUSE_VOLUME
            }
            else {
                1F
            }
        )

        val mainTrackClamp =
            if (shouldStop) {
                0F
            }
            else if (musicPlayer.isTrackPlaying(EVENT_TRACK)) {
                BACKGROUND_VOLUME
            }
            else if (isPaused) {
                PAUSE_VOLUME
            }
            else {
                1F
            }

        musicPlayer.clampTrackVolume(mainTrack, mainTrackClamp)
        musicPlayer.clampTrackVolume(getOldMainTrack(), mainTrackClamp)

        musicPlayer.clampTrackVolume(
            ambienceTrack,
            if (isPaused) {
                PAUSE_VOLUME
            }
            else {
                1F
            }
        )

        musicPlayer.tick()

        val isAmbiencePlaying = musicPlayer.isTrackPlaying(ambienceTrack)
        val isAmbienceAlmostDone = musicPlayer.isTrackAlmostDone(ambienceTrack)
            if ((ambienceToPlay.isEmpty() || client.player == null) && isAmbiencePlaying) {
            musicPlayer.stop(ambienceTrack)
        }

        if (!ambienceToPlay.isEmpty() &&
            client.player != null &&
            (!isAmbiencePlaying || !ambienceToPlay.contains(lastAmbience) || isAmbienceAlmostDone)) {
            val newAmbience = getPseudoRandomTrack(ambienceToPlay, lastAmbience)
            playNextAmbience(newAmbience)
            lastAmbience = newAmbience
        }

        if (shouldStop) {
            client.musicTracker.setCurrent(null)
            return
        }

        if (playingEvent != null && !musicPlayer.isTrackPlaying(EVENT_TRACK)) {
            playingEvent = null
        }

        if (playingEvent != null && !playingEvent!!.parameters.isPersistent && !eventPool.contains(playingEvent)) {
            musicPlayer.stop(EVENT_TRACK)
        }

        if (!shouldPlay(identifier)) {
            return
        }

        if (identifier != currentMusicPredicateId &&
            treeResult.accumulatedEvents.any { event -> event is OnEnterPredicateEvent }) {
            invokeMusicEvent(OnEnterPredicateEvent::class)
        }

        updatePredicateId(identifier)

        if (shouldKeepPlaying(musicToPlay, enterDelay, isEnter, persistentNodeMusic)) {
            return
        }

        val delay = if (isEnter) enterDelay else getRandomDelay(trackDelay, trackDelayNoise)
        val newMusic = getPseudoRandomTrack(musicToPlay, lastMusic)
        playNextMusic(
            newMusic,
            delay,
            shouldResume,
            !isEnter,
            loopMusic,
            loopStartPoints[newMusic.getSoundName()] ?: 0U
        )
    }

    private fun shouldPlay(identifier: String): Boolean {
        return (identifier != currentMusicPredicateId
                || (!musicPlayer.isTrackPlaying(mainTrack)
                        && !musicPlayer.isTrackDelayed(mainTrack)))
                && musicVolumeOption.value > 0
    }

    private fun shouldKeepPlaying(
        musicToPlay: List<PlayableSound>, enterDelay: UInt, isEnter: Boolean, persistentNodeMusic: Boolean): Boolean {
        val mainTrackPlaying = musicPlayer.isTrackPlaying(mainTrack)
        return mainTrackPlaying && (
                (musicToPlay.contains(lastMusic) && enterDelay != 0U)
                        || (persistentNodeMusic && isEnter)
                )
    }

    private fun getRandomDelay(trackDelay: UInt, trackDelayNoise: UInt): UInt {
        return max(
            0,
            (trackDelay.toInt() - trackDelayNoise.toInt()..trackDelay.toInt() + trackDelayNoise.toInt())
                .random())
            .toUInt()
    }

    private fun updatePredicateId(newIdentifier: String) {
        oldMusicPredicateId =
            if (newIdentifier != currentMusicPredicateId)
                currentMusicPredicateId
            else
                oldMusicPredicateId
        currentMusicPredicateId = newIdentifier
    }

    private fun playNextMusic(
        newMusic: PlayableSound,
        delay: UInt,
        resume: Boolean,
        keepTrack: Boolean,
        loopMusic: Boolean,
        loopIntroEndpoint: UInt
    ) {
        val delayMillis = delay.toLong() * 1000L
        if (keepTrack) {
            musicPlayer.startNew(
                mainTrack,
                newMusic,
                delayMillis,
                isLooping = loopMusic,
                loopStartPoint = loopIntroEndpoint
            )

            return
        }

        val oldTrack = mainTrack
        swapMainTrack()
        if (resume && musicPlayer.isTrackPlaying(mainTrack)) {
            musicPlayer.crossfadeTracks(oldTrack, mainTrack)
            return
        }

        musicPlayer.startNew(
            mainTrack, newMusic, delayMillis, isLooping = loopMusic, loopStartPoint = loopIntroEndpoint)
        musicPlayer.crossfadeTracks(oldTrack, mainTrack)

        lastMusic = newMusic
    }

    private fun playNextAmbience(newAmbience: PlayableSound) {
        val oldTrack = ambienceTrack
        swapAmbienceTrack()

        musicPlayer.startNew(ambienceTrack, newAmbience, fadeIn = true)
        musicPlayer.crossfadeTracks(oldTrack, ambienceTrack)

        lastAmbience = newAmbience
    }

    private fun swapMainTrack() {
        musicPlayer.cancelDelayedMusic(mainTrack)
        mainTrack = getOldMainTrack()
    }

    private fun swapAmbienceTrack() {
        ambienceTrack = getOldAmbienceTrack()
    }

    private fun getOldMainTrack(): String {
        return if (mainTrack == MAIN_TRACK_1) {
            MAIN_TRACK_2
        }
        else {
            MAIN_TRACK_1
        }
    }

    private fun getOldAmbienceTrack(): String {
        return if (ambienceTrack == AMBIENCE_TRACK_1) {
            AMBIENCE_TRACK_2
        }
        else {
            AMBIENCE_TRACK_1
        }
    }

    companion object {
        private const val MAIN_TRACK_1 = "main1"
        private const val MAIN_TRACK_2 = "main2"
        private const val AMBIENCE_TRACK_1 = "ambience1"
        private const val AMBIENCE_TRACK_2 = "ambience2"
        private const val EVENT_TRACK = "event"
        private const val ON_DEMAND_TRACK = "on_demand"
        private const val MAIN_CROSSFADE_TICKS = 75
        private const val ON_DEMAND_CROSSFADE_TICKS = 10
        private const val PAUSE_VOLUME = 0.3F
        private const val BACKGROUND_VOLUME = 0.1F

        private fun isPaused(client: MinecraftClient): Boolean {
            return client.world != null && client.currentScreen?.shouldPause() ?: false
        }

        private fun shouldStopMain(
            client: MinecraftClient, musicPlayer: MusicPlayer, musicToPlay: List<PlayableSound>): Boolean {
            return musicToPlay.isEmpty() ||
                    jukeboxPlaying(client) ||
                    musicPlayer.isTrackPlaying(ON_DEMAND_TRACK)
        }

        private fun jukeboxPlaying(client: MinecraftClient): Boolean {
            return client.soundManager.soundSystem.sources.keys.any {
                    instance ->
                ((instance.category == SoundCategory.RECORDS)
                        && (instance is PositionedSoundInstance)
                        && (client.player?.let {
                    Vec3d(instance.x, instance.y, instance.z)
                        .squaredDistanceTo(it.pos) <
                            (instance.sound?.attenuation ?: 0) * (instance.sound?.attenuation ?: 0) * 4
                } ?: false))
            }
        }

        private fun getPseudoRandomTrack(musicToPlay: List<PlayableSound>, lastMusic: PlayableSound?): PlayableSound {
            if (musicToPlay.size == 1) {
                return musicToPlay.first()
            }

            return (lastMusic?.let { musicToPlay.filterNot { it == lastMusic } } ?: musicToPlay).random()
        }
    }
}