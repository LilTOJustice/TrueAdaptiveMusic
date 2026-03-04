package liltojustice.trueadaptivemusic.client.javasucks.extensions

import liltojustice.trueadaptivemusic.client.TAMClient
import liltojustice.trueadaptivemusic.client.trigger.event.MusicEvent
import liltojustice.trueadaptivemusic.client.trigger.event.types.OnAdvancementGetEvent
import net.minecraft.client.sound.SoundInstance
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents

fun SoundInstance.shouldIgnore(): Boolean {
    return TAMClient.musicPack != null && (category == SoundCategory.MUSIC || uiToastCheck(this))
}

private fun uiToastCheck(sound: SoundInstance): Boolean {
    val events = TAMClient.currentPredicateResult?.accumulatedEvents ?: return false

    return sound.id.toString() == CHALLENGE_COMPLETE
            && events.stream().anyMatch { event: MusicEvent? -> event is OnAdvancementGetEvent }
}

private val CHALLENGE_COMPLETE = SoundEvents.UI_TOAST_CHALLENGE_COMPLETE.id.toString()
