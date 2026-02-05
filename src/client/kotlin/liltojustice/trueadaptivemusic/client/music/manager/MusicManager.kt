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
import net.minecraft.client.sound.SoundInstance
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

    init {
        musicPlayer.createTrack(
            MAIN_TRACK,
            allowResume = true,
            crossFadeTicks = MAIN_CROSSFADE_TICKS)
        musicPlayer.createTrack(
            EVENT_TRACK,
            allowResume = false,
            crossFadeTicks = ON_DEMAND_CROSSFADE_TICKS)
        musicPlayer.createTrack(
            ON_DEMAND_TRACK,
            allowResume = false,
            crossFadeTicks = ON_DEMAND_CROSSFADE_TICKS)

        InvokeMusicEventCallback.EVENT.register { eventType, args ->
            activeEvents.firstOrNull { event ->
                eventType == event.getTypeName()
                        && runCatching { event.validate(*args) }.getOrNull() == true }
                ?.let { event ->
                    event.playableSounds.randomOrNull()?.let {
                        musicPlayer.startNew(EVENT_TRACK, it)
                    }
                    playingEvent = event
                }

            ActionResult.PASS
        }
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

        val isPaused = isPaused(client)
        musicPlayer.clampTrackVolume(EVENT_TRACK,
            if (isPaused) {
                PAUSE_VOLUME
            }
            else {
                1F
            })

        musicPlayer.clampTrackVolume(MAIN_TRACK,
            if (musicPlayer.isTrackPlaying(EVENT_TRACK)) {
                BACKGROUND_VOLUME
            }
            else if (musicPlayer.isTrackPlaying(ON_DEMAND_TRACK)) {
                0F
            }
            else if (isPaused) {
                PAUSE_VOLUME
            }
            else {
                1F
            })

        musicPlayer.tick()

        if (musicPlayer.isTrackPlaying(ON_DEMAND_TRACK)) {
            return
        }

        val predicateResult = TAMClient.currentPredicateResult ?: return
        val identifier = predicateResult.path
        val parameters = predicateResult.predicateParameters
        val musicToPlay = predicateResult.music
        val trackDelayNoise = parameters.trackDelayNoise
        val trackDelay = parameters.trackDelay
        val enterDelay = parameters.enterDelay

        activeEvents = predicateResult.events

        if (playingEvent != null && !musicPlayer.isTrackPlaying(EVENT_TRACK)) {
            playingEvent = null
        }

        if (playingEvent != null && !playingEvent!!.parameters.isPersistent && !activeEvents.contains(playingEvent)) {
            musicPlayer.stop(EVENT_TRACK)
        }

        if (musicToPlay.isEmpty() || jukeboxPlaying()) {
            musicPlayer.stop(MAIN_TRACK)
            return
        }

        if (!shouldPlay(identifier)) {
            return
        }

        val shouldResume = oldMusicPredicateId == identifier && enterDelay == 0U
        val isEnter = currentMusicPredicateId != identifier

        if (identifier != currentMusicPredicateId &&
            predicateResult.events.any { event -> event is OnEnterPredicateEvent }) {
            MusicEvent.invokeMusicEvent(TAMClient.eventRegistry[OnEnterPredicateEvent::class])
        }

        oldMusicPredicateId =
            if (identifier != currentMusicPredicateId)
                currentMusicPredicateId
            else
                oldMusicPredicateId
        currentMusicPredicateId = identifier

        if (shouldResume) {
            musicPlayer.resumeOld(MAIN_TRACK)
        }
        else {
            val delay = if (isEnter) enterDelay else getRandomDelay(trackDelay, trackDelayNoise)
            val newMusic = getPseudoRandomTrack(musicToPlay, lastMusic)
            musicPlayer.startNew(
                MAIN_TRACK,
                newMusic,
                delay.toLong() * 1000L)
            lastMusic = newMusic
        }

        musicPlayer.getTrackInstance(MAIN_TRACK)?.let {
            client.musicTracker.setCurrent(it)
            client.toastManager.onMusicTrackStart()
        }
    }

    fun hasSoundInstance(soundInstance: SoundInstance): Boolean {
        return musicPlayer.hasSoundInstance(soundInstance)
    }

    private fun shouldPlay(identifier: String): Boolean {
        return (identifier != currentMusicPredicateId ||
                (!musicPlayer.isTrackPlaying(MAIN_TRACK) &&
                        !musicPlayer.isTrackDelayed(MAIN_TRACK)))
                && musicVolumeOption.value > 0
    }

    private fun stop() {
        musicPlayer.stopAll()
        currentMusicPredicateId = ""
        oldMusicPredicateId = ""
        activeEvents = emptyList()
        lastMusic = null
    }

    private fun jukeboxPlaying(): Boolean {
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

    private fun getRandomDelay(trackDelay: UInt, trackDelayNoise: UInt): UInt {
        return max(
            0,
            (trackDelay.toInt() - trackDelayNoise.toInt()..trackDelay.toInt() + trackDelayNoise.toInt()).random())
            .toUInt()
    }

    companion object {
        private const val MAIN_TRACK = "main"
        private const val EVENT_TRACK = "event"
        private const val ON_DEMAND_TRACK = "on_demand"
        private const val MAIN_CROSSFADE_TICKS = 50
        private const val ON_DEMAND_CROSSFADE_TICKS = 10
        private const val PAUSE_VOLUME = 0.3F
        private const val BACKGROUND_VOLUME = 0.1F

        private fun isPaused(client: MinecraftClient): Boolean {
            return client.world != null && client.currentScreen?.shouldPause() ?: false
        }

        private fun getPseudoRandomTrack(
            musicToPlay: List<PlayableSound>, lastMusic: PlayableSound? = null): PlayableSound {
            if (musicToPlay.size == 1) {
                return musicToPlay.first()
            }

            val remainingMusic = lastMusic?.let { musicToPlay.filterNot { it == lastMusic } } ?: musicToPlay

            return remainingMusic.random()
        }
    }
}