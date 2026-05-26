package liltojustice.trueadaptivemusic.client.music.manager

import liltojustice.trueadaptivemusic.Logger
import liltojustice.trueadaptivemusic.client.TAMClient
import liltojustice.trueadaptivemusic.client.music.pack.MusicLoadException
import liltojustice.trueadaptivemusic.client.sound.engine.VolumeManager
import liltojustice.trueadaptivemusic.client.sound.instance.TAMSoundInstance
import liltojustice.trueadaptivemusic.client.sound.engine.SoundSystem
import liltojustice.trueadaptivemusic.client.sound.playable.PlayableSound
import net.minecraft.client.MinecraftClient
import java.util.Timer
import java.util.TimerTask
import kotlin.concurrent.schedule
import kotlin.math.min

internal class MusicPlayer(private val client: MinecraftClient) {
    private val soundSystem = SoundSystem(client.options)
    private val volumeManager = VolumeManager(soundSystem)
    private val tracks = mutableMapOf<String, Track>()
    private val lock = Any()

    fun refreshSoundVolume() {
        soundSystem.refreshSoundVolume()
    }

    fun getPlayingInstance(trackName: String): TAMSoundInstance? {
        return getTrack(trackName)?.takeUnless { it.isDelayed() || !isTrackPlaying(it) }?.currentSoundInstance
    }

    fun createTrack(trackName: String, isAmbient: Boolean, crossFadeTicks: Int, allowPause: Boolean = true) {
        tracks[trackName] = Track(isAmbient, crossFadeTicks, allowPause)
    }

    fun removeTrack(trackName: String) {
        tracks.remove(trackName)?.let { stop(it) }
    }

    fun isTrackPlaying(trackName: String): Boolean {
        return getTrack(trackName)?.let { isTrackPlaying(it) } ?: false
    }

    fun isTrackDelayed(trackName: String): Boolean {
        return getTrack(trackName)?.let { isTrackDelayed(it) } ?: false
    }

    fun isTrackAlmostDone(trackName: String): Boolean {
        return getTrack(trackName)?.let { isTrackAlmostDone(it) } ?: false
    }

    fun tick() {
        tracks.values.forEach { track ->
            val currentSoundInstance = track.currentSoundInstance ?: return@forEach
            val currentVolume = currentSoundInstance.desiredVolume
            if (currentVolume > track.clampedVolume && !volumeManager.hasDownFade(currentSoundInstance) ) {
                volumeManager.startFade(
                    currentSoundInstance,
                    CLAMP_TICKS,
                    track.clampedVolume,
                    false,
                    track.allowPause
                )
            }
            else if (currentVolume < track.clampedVolume &&
                currentVolume < track.desiredVolume &&
                !volumeManager.hasUpFade(currentSoundInstance)) {
                volumeManager.startFade(
                    currentSoundInstance,
                    CLAMP_TICKS,
                    min(track.clampedVolume, track.desiredVolume),
                    false,
                    track.allowPause
                )
            }
        }

        volumeManager.tick()
        soundSystem.tick(client.player?.yaw)
    }

    fun crossfadeTracks(fadeOutTrackName: String, fadeInTrackName: String) {
        val fadeOutTrack = getTrack(fadeOutTrackName) ?: return
        val fadeInTrack = getTrack(fadeInTrackName) ?: return
        beginCrossfade(fadeOutTrack, fadeInTrack)
    }

    fun startNew(
        trackName: String,
        newMusic: PlayableSound,
        delayMillis: Long = 0L,
        fadeIn: Boolean = false,
        isLooping: Boolean = false,
        loopStartPoint: UInt = 0U
    ) {
        val track = getTrack(trackName) ?: return
        track.currentSoundInstance?.let {
            volumeManager.startFade(
                it, track.crossFadeTicks, 0F, true)
        }
        val newInstance = newMusic.makeSoundInstance(track.isAmbient, isLooping, loopStartPoint) ?: return

        track.updateSound(newMusic, newInstance)
        track.startDelay(delayMillis) { startNewInstance(track, newMusic, fadeIn, isLooping, loopStartPoint) }
    }

    fun stop(trackName: String) {
        getTrack(trackName)?.let { stop(it) }
    }

    fun stopAll() {
        volumeManager.clearFades()
        soundSystem.stopAll()
        tracks.values.forEach { track -> track.resetSounds() }
    }

    fun clampTrackVolume(trackName: String, clamp: Float) {
        getTrack(trackName)?.let { clampTrackVolume(it, clamp) }
    }

    fun setTrackVolume(trackName: String, volume: Float, allowPause: Boolean = false) {
        val track = getTrack(trackName) ?: return
        track.clampedVolume = volume
        track.currentSoundInstance?.let { volumeManager.setInstanceVolume(it, volume, allowPause) }
    }

    fun cancelDelayedMusic(trackName: String) {
        getTrack(trackName)?.cancelDelay()
    }

    private fun stop(track: Track) {
        synchronized(lock) {
            if (isTrackPlaying(track)) {
                track.currentSoundInstance?.let {
                    volumeManager.startFade(
                        it, track.crossFadeTicks, 0F, true)
                }

                track.resetSounds()
            }
        }
    }

    private fun startNewInstance(
        track: Track, newMusic: PlayableSound, fadeIn: Boolean, isLooping: Boolean, loopStartPoint: UInt) {
        synchronized(lock) {
            soundSystem.stop(track.currentSoundInstance)
            val newInstance = newMusic.makeSoundInstance(track.isAmbient, isLooping, loopStartPoint)
                ?: return@synchronized
            track.updateSound(newMusic, newInstance)
            playInstance(newInstance)

            if (fadeIn) {
                volumeManager.setInstanceVolume(newInstance, 0F, false)
                volumeManager.startFade(
                    newInstance, track.crossFadeTicks, track.clampedVolume)
            }
            else {
                volumeManager.setInstanceVolume(newInstance, track.clampedVolume)
            }

            track.desiredVolume = 1F
        }
    }

    private fun getTrack(trackName: String): Track? {
        return tracks[trackName]
    }

    private fun isTrackPlaying(track: Track): Boolean {
        return soundSystem.isPlaying(track.currentSoundInstance)
    }

    private fun isTrackDelayed(track: Track): Boolean {
        return track.isDelayed()
    }

    private fun isTrackAlmostDone(track: Track): Boolean {
        return soundSystem.instanceHasSecondsLeft(
            track.currentSoundInstance, track.crossFadeTicks.toFloat() / TAMClient.TPS)
    }

    private fun playInstance(soundInstance: TAMSoundInstance) {
        synchronized(lock) {
            try {
                soundSystem.play(soundInstance)
            }
            catch (e: MusicLoadException) {
                Logger.logError("Error: Failed to play sound instance - ${e.message}")
            }
        }
    }

    private fun beginCrossfade(fadeOutTrack: Track, fadeInTrack: Track) {
        synchronized(lock) {
            fadeOutTrack.desiredVolume = 0F
            fadeInTrack.desiredVolume = 1F
            fadeInTrack.currentSoundInstance
                ?.takeIf { it.desiredVolume == 0F || it.desiredVolume == 1F }
                ?.let { fadeInInstance ->
                    volumeManager.setInstanceVolume(fadeInInstance, 0.01F)
                }

            fadeInTrack.currentSoundInstance?.let {
                soundSystem.resumeInstance(it)
                volumeManager.startFade(
                    it,
                    fadeInTrack.crossFadeTicks,
                    fadeInTrack.clampedVolume,
                    false
                )
            }

            fadeOutTrack.currentSoundInstance?.let {
                volumeManager.startFade(
                    it,
                    fadeOutTrack.crossFadeTicks,
                    0F,
                    false
                )
            }
        }
    }

    private fun clampTrackVolume(track: Track, clamp: Float) {
        track.clampedVolume = clamp
    }

    companion object {
        private const val CLAMP_TICKS = 20
    }

    private class Track(val isAmbient: Boolean, val crossFadeTicks: Int, val allowPause: Boolean) {
        var currentSound: PlayableSound? = null
            private set
        var currentSoundInstance: TAMSoundInstance? = null
            private set
        var clampedVolume: Float = 1F
        var desiredVolume: Float = 1F

        private val delayTimer = Timer()
        private var delayTimerTask: TimerTask? = null

        fun startDelay(delayMillis: Long, onFinishDelay: (Track) -> Unit) {
            cancelDelay()
            if (delayMillis == 0L) {
                onFinishDelay(this)

                return
            }

            delayTimerTask = delayTimer.schedule(delayMillis) {
                onFinishDelay(this@Track)
                delayTimerTask = null
            }
        }

        fun isDelayed(): Boolean {
            return delayTimerTask != null
        }

        fun cancelDelay() {
            delayTimerTask?.cancel()
            delayTimerTask = null
        }

        fun updateSound(newSound: PlayableSound, newSoundInstance: TAMSoundInstance) {
            currentSound = newSound
            currentSoundInstance = newSoundInstance
        }

        fun resetSounds() {
            currentSound = null
            currentSoundInstance = null
        }
    }
}