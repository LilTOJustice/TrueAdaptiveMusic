package liltojustice.trueadaptivemusic.client.music.manager

import liltojustice.trueadaptivemusic.Logger
import liltojustice.trueadaptivemusic.client.music.pack.MusicLoadException
import liltojustice.trueadaptivemusic.client.sound.VolumeManager
import liltojustice.trueadaptivemusic.client.sound.instance.VolumeControlled
import liltojustice.trueadaptivemusic.client.sound.playable.PlayableSound
import liltojustice.trueadaptivemusic.client.sound.resumeInstance
import net.minecraft.client.MinecraftClient
import net.minecraft.client.sound.SoundInstance
import net.minecraft.client.sound.SoundManager
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

    fun createTrack(trackName: String, allowResume: Boolean, crossFadeTicks: Int) {
        tracks[trackName] = Track(soundManager, allowResume, crossFadeTicks)
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
            val currentVolume = (track.currentSoundInstance as? VolumeControlled ?: return@forEach).getVolume()
            if (currentVolume > track.clampedVolume &&
                track.currentSoundInstance?.let {
                    !volumeManager.hasFade(it) } == true ) {
                startFade(
                    track, CLAMP_TICKS, track.clampedVolume, stopWhenDone = false, isClamp = true)
            }
            else if (currentVolume < track.clampedVolume &&
                currentVolume < track.desiredVolume &&
                track.currentSoundInstance?.let {
                    !volumeManager.hasFade(it) } == true) {
                startFade(
                    track,
                    CLAMP_TICKS,
                    min(track.clampedVolume, track.desiredVolume),
                    stopWhenDone = false,
                    isClamp = true)
            }
        }

        volumeManager.tick()
    }

    fun resumeOld(trackName: String) {
        val track = getTrack(trackName)
        track.cancelDelay()
        track.oldSound?.let { sound ->
            track.oldSoundInstance?.let { instance ->
                beginCrossfade(track, sound, instance, true)
            }
        }
    }

    fun startNew(trackName: String, newMusic: PlayableSound, delayMillis: Long = 0L) {
        val track = getTrack(trackName)
        if (!isPlaying(track.currentSoundInstance)) {
            track.startDelay(delayMillis) { startNewInstance(track, newMusic) }
        }
        else if (delayMillis != 0L) {
            startFade(track, track.crossFadeTicks, 0F, false)
            track.updateSound(newMusic, newMusic.makeSoundInstance())
            track.startDelay(delayMillis) {
                track.desiredVolume = 1F
                soundManager.play(track.currentSoundInstance)
            }
        }
        else {
            track.startDelay(delayMillis) {
                beginCrossfade(track, newMusic, newMusic.makeSoundInstance(), false)
            }
        }
    }

    fun stop(trackName: String) {
        val track = getTrack(trackName)

        if (isTrackPlaying(track)) {
            startFade(track, track.crossFadeTicks, 0F, true)
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

    private fun startNewInstance(track: Track, newMusic: PlayableSound) {
        track.updateSound(newMusic, newMusic.makeSoundInstance())
        playInstance(track.currentSoundInstance)
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

    private fun startFade(
        track: Track, fadeTicks: Int, targetVolume: Float, stopWhenDone: Boolean, isClamp: Boolean = false) {
        track.currentSoundInstance?.let {
            volumeManager.startFade(it, fadeTicks, targetVolume, stopWhenDone)
            if (!isClamp) {
                track.desiredVolume = targetVolume
            }
        }
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
        track: Track, newSound: PlayableSound, newSoundInstance: SoundInstance, resume: Boolean) {
        track.updateSound(newSound, newSoundInstance)

        volumeManager.setInstanceVolume(track.currentSoundInstance!!, 0.01F)

        if (resume) {
            soundManager.resumeInstance(track.currentSoundInstance)
        }
        else {
            playInstance(track.currentSoundInstance)
        }

        startFade(track, track.crossFadeTicks, 1F, false)

        track.oldSoundInstance?.let {
            volumeManager.startFade(
                it,
                track.crossFadeTicks,
                0F,
                !track.allowResume)
        }
    }

    private fun clampTrackVolume(track: Track, clamp: Float) {
        track.clampedVolume = clamp
    }

    companion object {
        private const val CLAMP_TICKS = 20
    }

    private class Track(
        private val soundManager: SoundManager, val allowResume: Boolean, val crossFadeTicks: Int) {
        var currentSound: PlayableSound? = null
            private set
        var oldSound: PlayableSound? = null
            private set
        var currentSoundInstance: SoundInstance? = null
            private set
        var oldSoundInstance: SoundInstance? = null
            private set
        var clampedVolume: Float = 1F
        var desiredVolume: Float = 1F

        private val delayTimer = Timer()
        var delayTimerTask: TimerTask? = null

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
            return currentSoundInstance == soundInstance || oldSoundInstance == soundInstance
        }

        fun updateSound(newSound: PlayableSound, newSoundInstance: SoundInstance) {
            if (oldSound != newSound) {
                soundManager.stop(oldSoundInstance)
            }

            oldSound = currentSound
            oldSoundInstance = currentSoundInstance
            currentSound = newSound
            currentSoundInstance = newSoundInstance
        }

        fun resetSounds() {
            currentSound = null
            oldSound = null
            currentSoundInstance = null
            oldSoundInstance = null
        }
    }
}