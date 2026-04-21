package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusic.client.identifier.StatusEffectIdentifier
import liltojustice.trueadaptivemusicapi.trigger.TriggerArguments
import liltojustice.trueadaptivemusicapi.trigger.predicate.type.StaticPredicateType
import net.minecraft.client.Minecraft

class StatusEffectPredicate: StaticPredicateType<StatusEffectPredicate.Arguments>("status_effect") {
    override val argDescriptions: Map<String, String>
        get() = super.argDescriptions + mapOf(
            Arguments::statusEffects.name to "Which status effects the player needs to have for the music to play.")

    data class Arguments(val statusEffects: List<StatusEffectIdentifier>): TriggerArguments()

    override fun validate(arguments: Arguments): Boolean {
        val minecraft = Minecraft.getInstance()
        val playerStatusEffects = minecraft.player?.activeEffects ?: return false

        return arguments.statusEffects.any { statusEffect ->
            playerStatusEffects.any { playerStatusEffect -> playerStatusEffect.effect.`is`(statusEffect.id) }
        }
    }
}