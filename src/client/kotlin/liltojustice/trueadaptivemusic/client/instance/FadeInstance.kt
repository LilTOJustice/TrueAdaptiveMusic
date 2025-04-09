package liltojustice.trueadaptivemusic.client.instance

import net.minecraft.client.sound.SoundInstance

class FadeInstance(val soundInstance: SoundInstance, private val fadeIn: Boolean, private val totalTicks: Int = 50) {
    private var fadeTicks: Int = if (fadeIn) 0 else totalTicks

    fun tick(): Float {
        fadeTicks += if (fadeIn) 1 else -1
        return fadeTicks * 1f / totalTicks
    }

    fun done(): Boolean {
        return if (fadeIn) fadeTicks == totalTicks else fadeTicks == 0
    }
}