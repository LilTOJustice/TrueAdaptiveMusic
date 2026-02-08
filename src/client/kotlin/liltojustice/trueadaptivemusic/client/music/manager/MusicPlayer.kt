package liltojustice.trueadaptivemusic.client.music.manager

import liltojustice.trueadaptivemusic.Logger
import liltojustice.trueadaptivemusic.client.music.pack.MusicLoadException
import liltojustice.trueadaptivemusic.client.sound.VolumeManager
import liltojustice.trueadaptivemusic.client.sound.instance.VolumeControlled
import liltojustice.trueadaptivemusic.client.sound.playable.PlayableSound
import liltojustice.trueadaptivemusic.client.sound.resumeInstance
import net.minecraft.client.MinecraftClient
import net.minecraft.client.sound.SoundInstance
import net.minecraft.sound.SoundCategory
import java.util.Timer
import java.util.TimerTask
import kotlin.concurrent.schedule
import kotlin.math.min

internal class MusicPlayer(client: MinecraftClient) {
    private val soundManager = client.soundManager
    private val volumeManager = VolumeManager(soundManager) {
        client.options.getSoundVolume(SoundCategory.MUSIC)
    }
    private val tracks = mutableMapOf<String, Track>()

    fun getTrackInstance(trackName: String): SoundInstance? {
        return getTrack(trackName).currentSoundInstance
    }

    fun createTrack(trackName: String, crossFadeTicks: Int) {
        tracks[trackName] = Track(crossFadeTicks)
    }

    fun hasSoundInstance(instance: SoundInstance): Boolean {
        return tracks.values.any { track -> track.hasSoundInstance(instance) }
    }

    fun isTrackPlaying(trackName: String): Boolean {
        return isTrackPlaying(getTrack(trackName))
    }

    fun isTrackDelayed(trackName: String): Boolean {
        return isTrackDelayed(getTrack(trackName))
    }

    fun tick() {
        tracks.values.forEach { track ->
            val currentSoundInstance = track.currentSoundInstance ?: return@forEach
            val currentVolume = (track.currentSoundInstance as? VolumeControlled ?: return@forEach).getVolume()
            if (currentVolume > track.clampedVolume && !volumeManager.hasFade(currentSoundInstance) ) {
                volumeManager.startFade(
                    currentSoundInstance,
                    CLAMP_TICKS,
                    track.clampedVolume,
                    false)
            }
            else if (currentVolume < track.clampedVolume &&
                currentVolume < track.desiredVolume &&
                !volumeManager.hasFade(currentSoundInstance)) {
                volumeManager.startFade(
                    currentSoundInstance,
                    CLAMP_TICKS,
                    min(track.clampedVolume, track.desiredVolume),
                    false)
            }
        }

        volumeManager.tick()
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

    fun startNew(trackName: String, newMusic: PlayableSound, delayMillis: Long = 0L) {
        val track = getTrack(trackName)
        val newInstance = newMusic.makeSoundInstance()
        soundManager.stop(track.currentSoundInstance)
        track.updateSound(newMusic, newInstance)
        track.startDelay(delayMillis) { startNewInstance(track, newMusic) }
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
        soundManager.stopAll()
        soundManager.close()
        tracks.values.forEach { track -> track.resetSounds() }
    }

    fun clampTrackVolume(trackName: String, clamp: Float) {
        clampTrackVolume(getTrack(trackName), clamp)
    }

    fun cancelDelayedMusic(trackName: String) {
        getTrack(trackName).cancelDelay()
    }

    private fun startNewInstance(track: Track, newMusic: PlayableSound) {
        val newInstance = newMusic.makeSoundInstance()
        soundManager.stop(track.currentSoundInstance)
        track.updateSound(newMusic, newInstance)
        playInstance(newInstance)
        volumeManager.setInstanceVolume(newInstance, track.clampedVolume)
        track.desiredVolume = 1F
    }

    private fun getTrack(trackName: String): Track {
        return tracks[trackName] ?: throw MusicManagerException("Couldn't find track with name '$trackName'.")
    }

    private fun isTrackPlaying(track: Track): Boolean {
        return isPlaying(track.currentSoundInstance)
    }

    private fun isTrackDelayed(track: Track): Boolean {
        return track.isDelayed()
    }

    private fun isPlaying(soundInstance: SoundInstance?): Boolean {
        return soundManager.isPlaying(soundInstance) &&
                !(soundManager.soundSystem.sources[soundInstance]?.isStopped ?: true)
    }

    private fun playInstance(soundInstance: SoundInstance?) {
        try {
            soundManager.play(soundInstance)
        }
        catch (e: MusicLoadException) {
            Logger.logError("Error: Failed to play sound instance - ${e.message}")
        }
    }

    private fun beginCrossfade(
        outSoundInstance: SoundInstance,
        inSoundInstance: SoundInstance,
        outFadeTicks: Int,
        inFadeTicks: Int,
        inVolume: Float) {
        volumeManager.setInstanceVolume(inSoundInstance, 0.01F)
        soundManager.resumeInstance(inSoundInstance)
        volumeManager.startFade(inSoundInstance, inFadeTicks, inVolume, false)
        volumeManager.startFade(outSoundInstance, outFadeTicks, 0F, false)
    }

    private fun clampTrackVolume(track: Track, clamp: Float) {
        track.clampedVolume = clamp
    }

    companion object {
        private const val CLAMP_TICKS = 20
    }

    private class Track(val crossFadeTicks: Int) {
        var currentSound: PlayableSound? = null
            private set
        var currentSoundInstance: SoundInstance? = null
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

        fun hasSoundInstance(soundInstance: SoundInstance): Boolean {
            return currentSoundInstance == soundInstance
        }

        fun updateSound(newSound: PlayableSound, newSoundInstance: SoundInstance) {
            currentSound = newSound
            currentSoundInstance = newSoundInstance
        }

        fun resetSounds() {
            currentSound = null
            currentSoundInstance = null
        }
    }
}