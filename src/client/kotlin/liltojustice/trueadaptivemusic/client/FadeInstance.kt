package liltojustice.trueadaptivemusic.client

import net.minecraft.client.sound.SoundInstance

class FadeInstance(val soundInstance: SoundInstance, val fadeIn: Boolean) {
    companion object {
        private val TOTAL_FADE_TICKS: Int = 50
    }

    private var fadeTicks: Int = if (fadeIn) 0 else TOTAL_FADE_TICKS

    fun tick(): Float {
        fadeTicks += if (fadeIn) 1 else -1
        return fadeTicks * 1f / TOTAL_FADE_TICKS
    }

    fun done(): Boolean {
        return if (fadeIn) fadeTicks == TOTAL_FADE_TICKS else fadeTicks == 0
    }
}