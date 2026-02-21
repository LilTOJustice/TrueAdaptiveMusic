package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusic.client.identifier.StatusEffectIdentifier
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.MinecraftClient

class StatusEffectPredicate(private val statusEffects: List<StatusEffectIdentifier>): MusicPredicate() {
    override fun test(): Boolean {
        val client = MinecraftClient.getInstance()
        val playerStatusEffects = client.player?.statusEffects ?: return false

        return statusEffects.any { statusEffect ->
            playerStatusEffects.any { playerStatusEffect ->
                playerStatusEffect.effectType.matchesId(statusEffect.id) } }
    }

    companion object: MusicPredicateCompanion {
        override val argDescriptions: Map<String, String>
            get() = super.argDescriptions + mapOf(
                "statusEffects" to "Which status effects the player needs to have for the music to play."
            )
    }
}