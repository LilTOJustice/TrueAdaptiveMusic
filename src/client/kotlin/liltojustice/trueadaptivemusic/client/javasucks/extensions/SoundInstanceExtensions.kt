package liltojustice.trueadaptivemusic.client.javasucks.extensions

import liltojustice.trueadaptivemusic.client.TAMClient
import liltojustice.trueadaptivemusic.client.trigger.event.MusicEvent
import liltojustice.trueadaptivemusic.client.trigger.event.types.OnAdvancementGetEvent
import net.minecraft.client.resources.sounds.SoundInstance
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource

fun SoundInstance.shouldIgnore(): Boolean {
    return TAMClient.musicPack != null &&
            !TAMClient.isCompatibilityMode() &&
            (this.source == SoundSource.MUSIC || uiToastCheck(this))
}

private fun uiToastCheck(sound: SoundInstance): Boolean {
    val events = TAMClient.currentPredicateResult?.accumulatedEvents ?: return false

    return sound.identifier.toString() == CHALLENGE_COMPLETE &&
            events.stream().anyMatch { event: MusicEvent<*>? -> event?.type is OnAdvancementGetEvent }
}

private val CHALLENGE_COMPLETE = SoundEvents.UI_TOAST_CHALLENGE_COMPLETE.location.toString()
