package liltojustice.trueadaptivemusic.client.sound.engine

import liltojustice.trueadaptivemusic.client.sound.instance.TAMSoundInstance
import kotlin.math.sin

class VolumeManager(private val soundSystem: SoundSystem) {
    private val fades: MutableMap<TAMSoundInstance, Fade> = mutableMapOf()

    fun startFade(
        soundInstance: TAMSoundInstance, ticksToComplete: Int, targetVolume: Float, stopWhenDone: Boolean = false) {
        soundSystem.resumeInstance(soundInstance)
        val existingFade = fades[soundInstance]
        if (existingFade != null) {
            existingFade.redirect(targetVolume, ticksToComplete, stopWhenDone)
        } else {
            fades[soundInstance] =
                Fade(soundInstance, ticksToComplete, targetVolume, stopWhenDone, soundSystem)
        }
    }

    fun hasDownFade(soundInstance: TAMSoundInstance): Boolean {
        return fades.values.any { it.soundInstance == soundInstance && it.targetVolume < it.startingVolume }
    }

    fun hasUpFade(soundInstance: TAMSoundInstance): Boolean {
        return fades.values.any { it.soundInstance == soundInstance && it.targetVolume > it.startingVolume }
    }

    fun tick() {
        fades.values.toList().forEach { fade ->
            processFade(fade)
        }
    }

    fun clearFades() {
        fades.clear()
    }

    private fun processFade(fade: Fade) {
        setInstanceVolume(fade.soundInstance, fade.tick())
        if (!fade.done()) {
            return
        }

        if (fade.stopWhenDone) {
            soundSystem.stop(fade.soundInstance)
        }

        fades.remove(fade.soundInstance)
    }

    fun setInstanceVolume(soundInstance: TAMSoundInstance, volume: Float, allowPause: Boolean = true) {
        soundSystem.setInstanceVolume(soundInstance, volume)

        if (allowPause && volume == 0F) {
            soundSystem.pauseInstance(soundInstance)
        }
    }

    private class Fade(
        val soundInstance: TAMSoundInstance,
        private var totalTicks: Int,
        var targetVolume: Float,
        var stopWhenDone: Boolean,
        soundSystem: SoundSystem
    ) {
        private var fadeTicks: Int = 0
        var startingVolume: Float =
            if (soundSystem.isInstancePaused(soundInstance))
                0F
            else soundInstance.desiredVolume
            private set

        fun tick(): Float {
            fadeTicks++

            if (done()) {
                return targetVolume
            }

            val sin = sin(Math.PI.toFloat() / 2 * fadeTicks.toFloat() / totalTicks)

            return (targetVolume - startingVolume) * sin * sin + startingVolume
        }

        fun redirect(targetVolume: Float, totalTicks: Int, stopWhenDone: Boolean) {
            this.startingVolume = soundInstance.desiredVolume
            this.targetVolume = targetVolume
            this.totalTicks = totalTicks
            this.stopWhenDone = stopWhenDone
            fadeTicks = 0
        }

        fun done(): Boolean {
            return fadeTicks == totalTicks
        }
    }
}