package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusic.client.identifier.StatusEffectIdentifier
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.Minecraft

class StatusEffectPredicate(private val statusEffects: List<StatusEffectIdentifier>): MusicPredicate() {
    override fun test(): Boolean {
        val minecraft = Minecraft.getInstance()
        val playerStatusEffects = minecraft.player?.activeEffects ?: return false

        return statusEffects.any { statusEffect ->
            playerStatusEffects.any { playerStatusEffect -> playerStatusEffect.effect.`is`(statusEffect.id) }
        }
    }

    companion object: MusicPredicateCompanion {
        override val argDescriptions: Map<String, String>
            get() = super.argDescriptions + mapOf(
                StatusEffectPredicate::statusEffects.name to "Which status effects the player needs to have for the " +
                        "music to play."
            )
    }
}