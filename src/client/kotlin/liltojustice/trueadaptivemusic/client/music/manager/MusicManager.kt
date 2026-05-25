package liltojustice.trueadaptivemusic.client.music.manager

import liltojustice.trueadaptivemusic.client.music.pack.MusicPackOptions
import liltojustice.trueadaptivemusic.client.music.tree.MusicTree
import liltojustice.trueadaptivemusic.client.sound.instance.TAMSoundInstance
import liltojustice.trueadaptivemusic.client.trigger.event.MusicEvent
import liltojustice.trueadaptivemusic.client.sound.playable.PlayableSound
import liltojustice.trueadaptivemusic.client.sound.playable.PlayableSoundEvent
import liltojustice.trueadaptivemusic.client.trigger.event.types.OnEnterNodeEvent
import liltojustice.trueadaptivemusicapi.trigger.event.input.EmptyEventInput
import liltojustice.trueadaptivemusicapi.trigger.event.input.EventInput
import liltojustice.trueadaptivemusicapi.trigger.event.type.EventTypeBase
import net.minecraft.client.MinecraftClient
import net.minecraft.client.option.SimpleOption
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvent
import net.minecraft.util.math.Vec3d
import kotlin.math.max

class MusicManager(private val minecraft: MinecraftClient) {
    var playingEvent: MusicEvent<*>? = null
    val currentMusic: TAMSoundInstance?
        get() = musicPlayer.getPlayingInstance(mainTrack)
    val currentAmbience: TAMSoundInstance?
        get() = musicPlayer.getPlayingInstance(ambienceTrack)
    val currentEventMusic: TAMSoundInstance?
        get() = musicPlayer.getPlayingInstance(EVENT_TRACK)

    private val musicPlayer = MusicPlayer(minecraft)
    private var currentNodeId: String = ""
    private var oldNodeId: String = ""
    private var lastIgnorePersistence = false
    private var musicVolumeOption: SimpleOption<Double> =
        minecraft.options.getSoundVolumeOption(SoundCategory.MUSIC)
    private var masterVolumeOption: SimpleOption<Double> =
        minecraft.options.getSoundVolumeOption(SoundCategory.MASTER)
    private var eventPool: List<MusicEvent<*>> = emptyList()
    private var mainTrack = MAIN_TRACK_1
    private var ambienceTrack = AMBIENCE_TRACK_1
    private val parallelTracks = mutableMapOf<PlayableSound, String>()
    private var musicPool = mutableSetOf<PlayableSound>()
    private var ambiencePool = mutableSetOf<PlayableSound>()
    private var vanillaSoundEvent: PlayableSoundEvent? = null
    private var compatibilityMode = false

    init {
        musicPlayer.createTrack(MAIN_TRACK_1, false, MAIN_CROSSFADE_TICKS)
        musicPlayer.createTrack(MAIN_TRACK_2, false, MAIN_CROSSFADE_TICKS)
        musicPlayer.createTrack(AMBIENCE_TRACK_1, true, MAIN_CROSSFADE_TICKS)
        musicPlayer.createTrack(AMBIENCE_TRACK_2, true, MAIN_CROSSFADE_TICKS)
        musicPlayer.createTrack(EVENT_TRACK, false, ON_DEMAND_CROSSFADE_TICKS)
        musicPlayer.createTrack(ON_DEMAND_TRACK, false, ON_DEMAND_CROSSFADE_TICKS)
    }

    fun invokeMusicEvent(eventType: EventTypeBase, input: EventInput) {
        eventPool.firstOrNull { event ->
            eventType == event.type && runCatching { event.validate(input) }.getOrNull() == true }
            ?.let { event ->
                event.music.randomOrNull()?.let {
                    musicPlayer.startNew(EVENT_TRACK, it)
                }

                playingEvent = event
            }
    }

    fun isCompatibilityMode(): Boolean {
        return compatibilityMode
    }

    fun refreshSoundVolume() {
        musicPlayer.refreshSoundVolume()
    }

    fun playNow(onDemandSound: PlayableSound?) {
        onDemandSound?.let { musicPlayer.startNew(ON_DEMAND_TRACK, onDemandSound) }
            ?: musicPlayer.stop(ON_DEMAND_TRACK)
    }

    fun stop() {
        closeParallelMusic()
        musicPlayer.stopAll()
        currentNodeId = ""
        oldNodeId = ""
        eventPool = emptyList()
    }

    fun setDesiredVanillaSoundEvent(soundEvent: SoundEvent) {
        vanillaSoundEvent = PlayableSoundEvent(soundEvent.id)
    }

    fun tick(treeResult: MusicTree.Result, packOptions: MusicPackOptions) {
        if (masterVolumeOption.value == 0.0) {
            return
        }

        val identifier = treeResult.path
        val parameters = treeResult.parameters
        val parallelMusic = parameters.parallelMusic
        val vanillaMusic = parameters.vanillaMusic && !parallelMusic
        val lastCompatibilityMode = compatibilityMode
        compatibilityMode = parameters.compatibilityMode && vanillaMusic
        val musicToPlay = treeResult.accumulatedMusic.takeIf { !vanillaMusic }
            ?: vanillaSoundEvent?.let { listOf(it) }
            ?: emptyList()
        val ambienceToPlay = treeResult.accumulatedAmbience
        val trackDelayNoise = parameters.trackDelayNoise.takeIf { !parallelMusic } ?: 0U
        val trackDelay = parameters.trackDelay.takeIf { !parallelMusic } ?: 0U
        val enterDelay = parameters.enterDelay.takeIf { !parallelMusic } ?: 0U
        val loopMusic = (parameters.loopMusic || parallelMusic) && !vanillaMusic
        val loopStartPoints = parameters.loopStartPoints
        val shouldResume = oldNodeId == identifier && enterDelay == 0U
        val isEnter = currentNodeId != identifier
        val persistNodeMusic = packOptions.persistentNodeMusic &&
                (!parameters.ignorePersistence && !lastIgnorePersistence) &&
                !loopMusic &&
                isEnter

        if (isEnter) {
            musicPool.clear()
            ambiencePool.clear()
            lastIgnorePersistence = parameters.ignorePersistence
        }

        if (!compatibilityMode && lastCompatibilityMode) {
            vanillaSoundEvent?.let { minecraft.soundManager.stopSounds(it.getId(), SoundCategory.MUSIC) }
        }

        eventPool = treeResult.accumulatedEvents

        val isPaused = isPaused(minecraft)
        val shouldStop = compatibilityMode ||
                packOptions.prioritySoundEvents.any { it.id == vanillaSoundEvent?.getId() } ||
                shouldStopMain(minecraft, musicPlayer, musicToPlay)

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

        val finalClamp = mainTrackClamp.takeIf { parallelTracks.isEmpty() } ?: 0F
        musicPlayer.clampTrackVolume(mainTrack, finalClamp)
        musicPlayer.clampTrackVolume(getOldMainTrack(), finalClamp)

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
        if ((ambienceToPlay.isEmpty() || minecraft.player == null) && isAmbiencePlaying) {
            musicPlayer.stop(ambienceTrack)
        }

        if (!ambienceToPlay.isEmpty() &&
            minecraft.player != null &&
            (!isAmbiencePlaying || !ambienceToPlay.contains(currentAmbience?.playableSound) || isAmbienceAlmostDone)) {
            val newAmbience = getPseudoRandomAmbience(ambienceToPlay)
            playNextAmbience(newAmbience)
        }


        if (playingEvent != null && !musicPlayer.isTrackPlaying(EVENT_TRACK)) {
            playingEvent = null
        }

        if (playingEvent != null && !playingEvent!!.parameters.isPersistent && !eventPool.contains(playingEvent)) {
            musicPlayer.stop(EVENT_TRACK)
        }

        if (isEnter && treeResult.accumulatedEvents.any { event -> event.type is OnEnterNodeEvent }) {
            invokeMusicEvent(OnEnterNodeEvent, EmptyEventInput())
        }

        if (shouldStop) {
            closeParallelMusic()

            return
        }

        treeResult.parallelMusicContext?.let { context ->
            musicToPlay.firstOrNull()?.let {
                handleParallelMusic(it, context, mainTrackClamp)

                return
            }
        }

        closeParallelMusic()

        if (!shouldPlay(identifier)) {
            return
        }

        updatePredicateId(identifier)

        if (shouldKeepPlaying(musicToPlay, persistNodeMusic)) {
            return
        }

        val delay = if (isEnter) enterDelay else getRandomDelay(trackDelay, trackDelayNoise)
        val newMusic = getPseudoRandomMusic(musicToPlay)
        playNextMusic(
            newMusic,
            delay,
            shouldResume,
            !isEnter,
            loopMusic,
            loopStartPoints[newMusic.getSoundName()] ?: 0U
        )
    }

    private fun handleParallelMusic(
        currentMusic: PlayableSound, context: MusicTree.ParallelMusicContext, mainTrackClamp: Float) {
        context.parallelMusic.forEach { music ->
            val trackName = parallelTrack(music)
            if (music in parallelTracks) {
                return@forEach
            }

            parallelTracks[music] = trackName
            musicPlayer.createTrack(trackName, false, PARALLEL_CROSSFADE_TICKS, false)
            musicPlayer.startNew(trackName, music, isLooping = true, loopStartPoint = context.loopStartPoint)

            if (music != currentMusic) {
                musicPlayer.setTrackVolume(trackName, 0F)
            }
        }

        parallelTracks.forEach { music, trackName ->
            if (music != currentMusic) {
                musicPlayer.clampTrackVolume(trackName, 0F)
            }
            else {
                musicPlayer.clampTrackVolume(trackName, mainTrackClamp)
            }
        }
    }

    private fun closeParallelMusic() {
        parallelTracks.values.forEach { musicPlayer.removeTrack(it) }
        parallelTracks.clear()
    }

    private fun shouldPlay(identifier: String): Boolean {
        return (identifier != currentNodeId
                || (!musicPlayer.isTrackPlaying(mainTrack)
                && !musicPlayer.isTrackDelayed(mainTrack)))
                && musicVolumeOption.value > 0
    }

    private fun shouldKeepPlaying(musicToPlay: List<PlayableSound>, persistNodeMusic: Boolean): Boolean {
        val mainTrackPlaying = musicPlayer.isTrackPlaying(mainTrack)
        return mainTrackPlaying && (musicToPlay.contains(currentMusic?.playableSound) || persistNodeMusic)
    }

    private fun getRandomDelay(trackDelay: UInt, trackDelayNoise: UInt): UInt {
        return max(
            0,
            (trackDelay.toInt() - trackDelayNoise.toInt()..trackDelay.toInt() + trackDelayNoise.toInt())
                .random())
            .toUInt()
    }

    private fun updatePredicateId(newIdentifier: String) {
        oldNodeId =
            if (newIdentifier != currentNodeId)
                currentNodeId
            else
                oldNodeId
        currentNodeId = newIdentifier
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
        minecraft.toastManager.onMusicTrackStart()
    }

    private fun playNextAmbience(newAmbience: PlayableSound) {
        val oldTrack = ambienceTrack
        swapAmbienceTrack()

        musicPlayer.startNew(ambienceTrack, newAmbience, fadeIn = true)
        musicPlayer.crossfadeTracks(oldTrack, ambienceTrack)
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

    private fun getPseudoRandomMusic(musicToPlay: List<PlayableSound>): PlayableSound {
        if (musicPool.isEmpty()) {
            musicPool = musicToPlay.toMutableSet()
        }

        val randomSound = musicPool.random()
        musicPool.remove(randomSound)

        return randomSound
    }

    private fun getPseudoRandomAmbience(ambienceToPlay: List<PlayableSound>): PlayableSound {
        if (ambiencePool.isEmpty()) {
            ambiencePool = ambienceToPlay.toMutableSet()
        }

        val randomSound = ambiencePool.random()
        ambiencePool.remove(randomSound)

        return randomSound
    }

    companion object {
        private const val MAIN_TRACK_1 = "main1"
        private const val MAIN_TRACK_2 = "main2"
        private const val AMBIENCE_TRACK_1 = "ambience1"
        private const val AMBIENCE_TRACK_2 = "ambience2"
        private const val EVENT_TRACK = "event"
        private const val ON_DEMAND_TRACK = "on_demand"
        private const val MAIN_CROSSFADE_TICKS = 75
        private const val PARALLEL_CROSSFADE_TICKS = 30
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

        private fun jukeboxPlaying(minecraft: MinecraftClient): Boolean {
            return try {
                minecraft.soundManager.soundSystem.sources.keys.any { instance ->
                    ((instance.category == SoundCategory.RECORDS)
                            && (instance is PositionedSoundInstance)
                            && (minecraft.player?.let {
                        Vec3d(instance.x, instance.y, instance.z)
                            .squaredDistanceTo(it.entityPos) <
                                (instance.sound?.attenuation ?: 0) * (instance.sound?.attenuation ?: 0) * 4
                    } ?: false))
                }
            }
            catch (_: ConcurrentModificationException) {
                false
            }
        }

        private fun parallelTrack(sound: PlayableSound): String {
            return "Parallel: ${sound.getSoundName()}"
        }
    }
}