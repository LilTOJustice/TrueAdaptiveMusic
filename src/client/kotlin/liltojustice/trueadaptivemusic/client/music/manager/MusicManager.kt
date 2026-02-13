package liltojustice.trueadaptivemusic.client.music.manager

import liltojustice.trueadaptivemusic.client.InvokeMusicEventCallback
import liltojustice.trueadaptivemusic.client.TAMClient
import liltojustice.trueadaptivemusic.client.music.pack.MusicPack
import liltojustice.trueadaptivemusic.client.trigger.event.MusicEvent
import liltojustice.trueadaptivemusic.client.sound.playable.PlayableSound
import liltojustice.trueadaptivemusic.client.trigger.event.types.OnEnterPredicateEvent
import net.minecraft.client.MinecraftClient
import net.minecraft.client.option.SimpleOption
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.sound.SoundCategory
import net.minecraft.util.ActionResult
import net.minecraft.util.math.Vec3d
import kotlin.math.max

class MusicManager(private val client: MinecraftClient) {
    var musicPack: MusicPack? = null
        private set
    var playingEvent: MusicEvent? = null
        private set

    private val musicPlayer = MusicPlayer(client)
    private var currentMusicPredicateId: String = ""
    private var oldMusicPredicateId: String = ""
    private var musicVolumeOption: SimpleOption<Double> =
        client.options.getSoundVolumeOption(SoundCategory.MUSIC)
    private var masterVolumeOption: SimpleOption<Double> =
        client.options.getSoundVolumeOption(SoundCategory.MASTER)
    private var activeEvents: List<MusicEvent> = emptyList()
    private var lastMusic: PlayableSound? = null
    private var lastAmbience: PlayableSound? = null
    private var mainTrack = MAIN_TRACK_1

    init {
        musicPlayer.createTrack(MAIN_TRACK_1, false, MAIN_CROSSFADE_TICKS)
        musicPlayer.createTrack(MAIN_TRACK_2, false, MAIN_CROSSFADE_TICKS)
        musicPlayer.createTrack(AMBIENCE_TRACK, true, MAIN_CROSSFADE_TICKS)
        musicPlayer.createTrack(EVENT_TRACK, false, ON_DEMAND_CROSSFADE_TICKS)
        musicPlayer.createTrack(ON_DEMAND_TRACK, false, ON_DEMAND_CROSSFADE_TICKS)

        InvokeMusicEventCallback.EVENT.register { eventType, args ->
            activeEvents.firstOrNull { event ->
                eventType == event.getTypeName()
                        && runCatching { event.validate(*args) }.getOrNull() == true }
                ?.let { event ->
                    event.music.randomOrNull()?.let {
                        musicPlayer.startNew(EVENT_TRACK, it)
                    }
                    playingEvent = event
                }

            ActionResult.PASS
        }
    }

    fun refreshSoundVolume() {
        musicPlayer.refreshSoundVolume()
    }

    fun playNow(onDemandSound: PlayableSound?) {
        onDemandSound?.let { musicPlayer.startNew(ON_DEMAND_TRACK, onDemandSound) }
            ?: musicPlayer.stop(ON_DEMAND_TRACK)
    }

    fun selectMusicPack(musicPack: MusicPack?) {
        stop()
        this.musicPack = musicPack
    }

    fun tick() {
        if (masterVolumeOption.value == 0.0) {
            return
        }

        val predicateResult = TAMClient.currentPredicateResult ?: return
        val identifier = predicateResult.path
        val parameters = predicateResult.predicateParameters
        val musicToPlay = predicateResult.music
        val ambienceToPlay = predicateResult.ambience
        val trackDelayNoise = parameters.trackDelayNoise
        val trackDelay = parameters.trackDelay
        val enterDelay = parameters.enterDelay
        val shouldResume = oldMusicPredicateId == identifier && enterDelay == 0U
        val isEnter = currentMusicPredicateId != identifier

        activeEvents = predicateResult.events

        val isPaused = isPaused(client)
        val shouldStop = shouldStopMain(client, musicPlayer, musicToPlay)

        musicPlayer.clampTrackVolume(EVENT_TRACK,
            if (isPaused) {
                PAUSE_VOLUME
            }
            else {
                1F
            })

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
        musicPlayer.clampTrackVolume(getOldTrack(), mainTrackClamp)

        musicPlayer.clampTrackVolume(AMBIENCE_TRACK,
            if (isPaused) {
                PAUSE_VOLUME
            }
            else {
                1F
            })

        musicPlayer.tick()

        val isAmbiencePlaying = musicPlayer.isTrackPlaying(AMBIENCE_TRACK)
        if (ambienceToPlay.isEmpty() && isAmbiencePlaying) {
            musicPlayer.stop(AMBIENCE_TRACK)
        }

        if (!ambienceToPlay.isEmpty() && (!isAmbiencePlaying || !ambienceToPlay.contains(lastAmbience))) {
            val newAmbience = getPseudoRandomTrack(ambienceToPlay, lastAmbience)
            musicPlayer.startNew(
                AMBIENCE_TRACK,
                getPseudoRandomTrack(ambienceToPlay, lastAmbience))
            lastAmbience = newAmbience
        }

        if (shouldStop) {
            client.musicTracker.setCurrent(null)
            return
        }

        if (playingEvent != null && !musicPlayer.isTrackPlaying(EVENT_TRACK)) {
            playingEvent = null
        }

        if (playingEvent != null && !playingEvent!!.parameters.isPersistent && !activeEvents.contains(playingEvent)) {
            musicPlayer.stop(EVENT_TRACK)
        }

        if (!shouldPlay(identifier)) {
            return
        }

        if (identifier != currentMusicPredicateId &&
            predicateResult.events.any { event -> event is OnEnterPredicateEvent }) {
            MusicEvent.invokeMusicEvent(TAMClient.eventRegistry[OnEnterPredicateEvent::class])
        }

        updatePredicateId(identifier)

        if (musicPlayer.isTrackPlaying(mainTrack) && musicToPlay.contains(lastMusic) && enterDelay != 0U) {
            return
        }

        val delay = if (isEnter) enterDelay else getRandomDelay(trackDelay, trackDelayNoise)
        val newMusic = getPseudoRandomTrack(musicToPlay, lastMusic)
        playNextMusic(newMusic, delay, shouldResume, !isEnter)

        musicPlayer.getTrackInstance(mainTrack)?.let {
            client.musicTracker.setCurrent(it)
            client.toastManager.onMusicTrackStart()
        }
    }

    private fun shouldPlay(identifier: String): Boolean {
        return (identifier != currentMusicPredicateId
                || (!musicPlayer.isTrackPlaying(mainTrack)
                        && !musicPlayer.isTrackDelayed(mainTrack)))
                && musicVolumeOption.value > 0
    }

    fun stop() {
        client.musicTracker.setCurrent(null)
        musicPlayer.stopAll()
        currentMusicPredicateId = ""
        oldMusicPredicateId = ""
        activeEvents = emptyList()
        lastMusic = null
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

    private fun playNextMusic(newMusic: PlayableSound, delay: UInt, resume: Boolean, keepTrack: Boolean) {
        val delayMillis = delay.toLong() * 1000L
        if (keepTrack) {
            musicPlayer.startNew(mainTrack, newMusic, delayMillis)
            return
        }

        val oldTrack = mainTrack
        swapTracks()
        if (resume && musicPlayer.isTrackPlaying(mainTrack)) {
            musicPlayer.crossfadeTracks(oldTrack, mainTrack)
            return
        }

        musicPlayer.startNew(mainTrack, newMusic, delayMillis)
        musicPlayer.crossfadeTracks(oldTrack, mainTrack)

        lastMusic = newMusic
    }

    private fun swapTracks() {
        musicPlayer.cancelDelayedMusic(mainTrack)
        mainTrack = getOldTrack()
    }

    private fun getOldTrack(): String {
        return if (mainTrack == MAIN_TRACK_1) {
            MAIN_TRACK_2
        }
        else {
            MAIN_TRACK_1
        }
    }

    companion object {
        private const val MAIN_TRACK_1 = "main1"
        private const val MAIN_TRACK_2 = "main2"
        private const val AMBIENCE_TRACK = "ambience"
        private const val EVENT_TRACK = "event"
        private const val ON_DEMAND_TRACK = "on_demand"
        private const val MAIN_CROSSFADE_TICKS = 50
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
                        .squaredDistanceTo(it.entityPos) <
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