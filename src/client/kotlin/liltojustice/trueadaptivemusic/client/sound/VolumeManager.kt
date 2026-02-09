package liltojustice.trueadaptivemusic.client.sound

import liltojustice.trueadaptivemusic.client.sound.instance.VolumeControlled
import net.minecraft.client.sound.SoundInstance
import net.minecraft.client.sound.SoundManager
import net.minecraft.sound.SoundCategory
import kotlin.math.sin

class VolumeManager(
    private val soundManager: SoundManager, private val getSoundVolume: (category: SoundCategory) -> Float) {
    private val fades: MutableMap<SoundInstance, Fade> = mutableMapOf()

    fun startFade(
        soundInstance: SoundInstance, ticksToComplete: Int, targetVolume: Float, stopWhenDone: Boolean = false) {
        soundManager.resumeInstance(soundInstance)
        val existingFade = fades[soundInstance]
        if (existingFade != null) {
            existingFade.redirect(targetVolume, ticksToComplete, stopWhenDone)
        } else {
            fades[soundInstance] =
                Fade(soundInstance, ticksToComplete, targetVolume, stopWhenDone, soundManager)
        }
    }

    fun hasFade(soundInstance: SoundInstance): Boolean {
        return fades.values.any { it.soundInstance == soundInstance }
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
            soundManager.stop(fade.soundInstance)
        }

        fades.remove(fade.soundInstance)
    }

    fun setInstanceVolume(soundInstance: SoundInstance, volume: Float, allowPause: Boolean = true) {
        soundManager.setInstanceVolume(soundInstance, volume, getSoundVolume(soundInstance.category))

        if (allowPause && volume == 0F) {
            soundManager.pauseInstance(soundInstance)
        }
    }

    private class Fade(
        val soundInstance: SoundInstance,
        private var totalTicks: Int,
        private var targetVolume: Float,
        var stopWhenDone: Boolean,
        soundManager: SoundManager) {
        private var fadeTicks: Int = 0
        private var startingVolume: Float =
            if (soundManager.isInstancePaused(soundInstance))
                0F
            else getInstanceVolume(soundInstance)

        fun tick(): Float {
            fadeTicks++

            if (done()) {
                return targetVolume
            }

            val sin = sin(Math.PI.toFloat() / 2 * fadeTicks.toFloat() / totalTicks)

            return (targetVolume - startingVolume) * sin * sin + startingVolume
        }

        fun redirect(targetVolume: Float, totalTicks: Int, stopWhenDone: Boolean) {
            this.startingVolume = getInstanceVolume(soundInstance)
            this.targetVolume = targetVolume
            this.totalTicks = totalTicks
            this.stopWhenDone = stopWhenDone
            fadeTicks = 0
        }

        fun done(): Boolean {
            return fadeTicks == totalTicks
        }
    }

    companion object {
        fun getInstanceVolume(soundInstance: SoundInstance?): Float {
            return (soundInstance as? VolumeControlled)?.getVolume() ?: 1F
        }
    }
}
