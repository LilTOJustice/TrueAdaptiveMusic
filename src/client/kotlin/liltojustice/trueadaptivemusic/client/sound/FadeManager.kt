package liltojustice.trueadaptivemusic.client.sound

import liltojustice.trueadaptivemusic.client.sound.instance.VolumeControlled
import net.minecraft.client.option.SimpleOption
import net.minecraft.client.sound.SoundInstance
import net.minecraft.client.sound.SoundManager

class FadeManager(private val soundManager: SoundManager, private val musicVolumeOption: SimpleOption<Double>) {
    private val fades: MutableMap<SoundInstance, Fade> = mutableMapOf()

    fun startFade(
        soundInstance: SoundInstance, ticksToComplete: Int, targetVolume: Float, stopWhenDone: Boolean = false) {
        soundManager.resumeInstance(soundInstance)
        val existingFade = fades[soundInstance]
        if (existingFade != null) {
            existingFade.redirect(targetVolume, ticksToComplete, stopWhenDone)
        } else {
            fades[soundInstance] = Fade(soundInstance, ticksToComplete, targetVolume, stopWhenDone)
        }
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
        if (fade.done()) {
            if (fade.stopWhenDone) {
                soundManager.stop(fade.soundInstance)
            }

            fades.remove(fade.soundInstance)
        }
    }

    private fun setInstanceVolume(soundInstance: SoundInstance, volume: Float) {
        soundManager.setInstanceVolume(soundInstance, volume, musicVolumeOption)

        if (volume == 0F) {
            soundManager.pauseInstance(soundInstance)
        }
    }

    class Fade(
        val soundInstance: SoundInstance,
        private var totalTicks: Int,
        private var targetVolume: Float,
        var stopWhenDone: Boolean) {
        private var fadeTicks: Int = 0
        private var currentVolume: Float = getInstanceVolume(soundInstance)

        fun tick(): Float {
            fadeTicks++

            if (done()) {
                return targetVolume
            }

            val x = (fadeTicks * 1F / totalTicks)
            currentVolume += (targetVolume - currentVolume) / (totalTicks - fadeTicks) * x

            return currentVolume
        }

        fun redirect(targetVolume: Float, totalTicks: Int, stopWhenDone: Boolean) {
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
        private fun getInstanceVolume(soundInstance: SoundInstance?): Float {
            return (soundInstance as? VolumeControlled)?.getVolume() ?: 1F
        }
    }
}
