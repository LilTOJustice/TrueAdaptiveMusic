package liltojustice.trueadaptivemusic.client.instance

import net.minecraft.client.sound.SoundInstance

class FadeInstance(
    val soundInstance: SoundInstance,
    private val fadeIn: Boolean,
    private val totalTicks: Int = 50,
    private val minimumVolume: Float = 0F) {
    private var fadeTicks: Int = 0

    fun tick(): Float {
        fadeTicks++
        return if (fadeIn)
            (fadeTicks * 1F / totalTicks) * (1 - minimumVolume) + minimumVolume
        else
            (fadeTicks * 1F / totalTicks) * (minimumVolume - 1) + 1
    }

    fun done(): Boolean {
        return fadeTicks == totalTicks
    }
}