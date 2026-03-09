package liltojustice.trueadaptivemusic.client.music.manager

import liltojustice.trueadaptivemusic.Logger
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

    fun refreshSoundVolume() {
        soundSystem.refreshSoundVolume()
    }

    fun getPlayingInstance(trackName: String): TAMSoundInstance? {
        return getTrack(trackName).takeUnless { it.isDelayed() || !isTrackPlaying(it) }?.currentSoundInstance
    }

    fun createTrack(trackName: String, isAmbient: Boolean, crossFadeTicks: Int) {
        tracks[trackName] = Track(isAmbient, crossFadeTicks)
    }

    fun isTrackPlaying(trackName: String): Boolean {
        return isTrackPlaying(getTrack(trackName))
    }

    fun isTrackDelayed(trackName: String): Boolean {
        return isTrackDelayed(getTrack(trackName))
    }

    fun isTrackAlmostDone(trackName: String): Boolean {
        return isTrackAlmostDone(getTrack(trackName))
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
                    false)
            }
            else if (currentVolume < track.clampedVolume &&
                currentVolume < track.desiredVolume &&
                !volumeManager.hasUpFade(currentSoundInstance)) {
                volumeManager.startFade(
                    currentSoundInstance,
                    track.crossFadeTicks,
                    min(track.clampedVolume, track.desiredVolume),
                    false)
            }
        }

        volumeManager.tick()
        soundSystem.tick(client.player?.yaw)
    }

    fun crossfadeTracks(fadeOutTrackName: String, fadeInTrackName: String) {
        val fadeOutTrack = getTrack(fadeOutTrackName)
        val fadeInTrack = getTrack(fadeInTrackName)
        val fadeOutInstance = fadeOutTrack.currentSoundInstance ?: return
        val fadeInInstance = fadeInTrack.currentSoundInstance ?: return
        fadeOutTrack.desiredVolume = 0F
        fadeInTrack.desiredVolume = 1F
        beginCrossfade(
            fadeOutInstance,
            fadeInInstance,
            fadeOutTrack.crossFadeTicks,
            fadeInTrack.crossFadeTicks,
            fadeInTrack.clampedVolume)
    }

    fun startNew(
        trackName: String,
        newMusic: PlayableSound,
        delayMillis: Long = 0L,
        fadeIn: Boolean = false,
        isLooping: Boolean = false,
        loopStartPoint: UInt = 0U
    ) {
        val track = getTrack(trackName)
        val newInstance = newMusic.makeSoundInstance(track.isAmbient, isLooping, loopStartPoint)
        track.currentSoundInstance?.let {
            volumeManager.startFade(
                it, track.crossFadeTicks, 0F, true)
        }
        track.updateSound(newMusic, newInstance)
        track.startDelay(delayMillis) { startNewInstance(track, newMusic, fadeIn, isLooping, loopStartPoint) }
    }

    fun stop(trackName: String) {
        val track = getTrack(trackName)

        if (isTrackPlaying(track)) {
            track.currentSoundInstance?.let {
                volumeManager.startFade(
                    it, track.crossFadeTicks, 0F, true)
            }
            track.resetSounds()
        }
    }

    fun stopAll() {
        volumeManager.clearFades()
        soundSystem.stopAll()
        tracks.values.forEach { track -> track.resetSounds() }
    }

    fun clampTrackVolume(trackName: String, clamp: Float) {
        clampTrackVolume(getTrack(trackName), clamp)
    }

    fun cancelDelayedMusic(trackName: String) {
        getTrack(trackName).cancelDelay()
    }

    private fun startNewInstance(
        track: Track, newMusic: PlayableSound, fadeIn: Boolean, isLooping: Boolean, loopStartPoint: UInt) {
        val newInstance = newMusic.makeSoundInstance(track.isAmbient, isLooping, loopStartPoint)
        soundSystem.stop(track.currentSoundInstance)
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

    private fun getTrack(trackName: String): Track {
        return tracks[trackName] ?: throw MusicManagerException("Couldn't find track with name '$trackName'.")
    }

    private fun isTrackPlaying(track: Track): Boolean {
        return soundSystem.isPlaying(track.currentSoundInstance)
    }

    private fun isTrackDelayed(track: Track): Boolean {
        return track.isDelayed()
    }

    private fun isTrackAlmostDone(track: Track): Boolean {
        return soundSystem.isAlmostDone(track.currentSoundInstance)
    }

    private fun playInstance(soundInstance: TAMSoundInstance) {
        try {
            soundSystem.play(soundInstance)
        }
        catch (e: MusicLoadException) {
            Logger.logError("Error: Failed to play sound instance - ${e.message}")
        }
    }

    private fun beginCrossfade(
        outSoundInstance: TAMSoundInstance,
        inSoundInstance: TAMSoundInstance,
        outFadeTicks: Int,
        inFadeTicks: Int,
        inVolume: Float) {
        if (inSoundInstance.desiredVolume == 0F || inSoundInstance.desiredVolume == 1F) {
            volumeManager.setInstanceVolume(inSoundInstance, 0.01F)
        }

        soundSystem.resumeInstance(inSoundInstance)
        volumeManager.startFade(inSoundInstance, inFadeTicks, inVolume, false)
        volumeManager.startFade(outSoundInstance, outFadeTicks, 0F, false)
    }

    private fun clampTrackVolume(track: Track, clamp: Float) {
        track.clampedVolume = clamp
    }

    companion object {
        private const val CLAMP_TICKS = 20
    }

    private class Track(val isAmbient: Boolean, val crossFadeTicks: Int) {
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