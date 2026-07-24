package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusicapi.identifier.StatusEffectIdentifier
import liltojustice.trueadaptivemusicapi.trigger.arguments.TriggerArguments
import liltojustice.trueadaptivemusicapi.trigger.predicate.type.StaticPredicateType
import net.minecraft.client.Minecraft
import kotlin.reflect.typeOf

object StatusEffectPredicate: StaticPredicateType<StatusEffectPredicate.Arguments>(
    "status_effect", typeOf<Arguments>()
) {
    override val argDescriptions: Map<String, String>
        get() = super.argDescriptions + mapOf(
            Arguments::statusEffects.name to "Which status effects the player needs to have for the music to play.")

    data class Arguments(val statusEffects: List<StatusEffectIdentifier>): TriggerArguments()

    override fun test(arguments: Arguments): Boolean {
        val minecraft = Minecraft.getInstance()
        val playerStatusEffects = minecraft.player?.activeEffects ?: return false

        return arguments.statusEffects.any { statusEffect ->
            playerStatusEffects.any { playerStatusEffect ->
                statusEffect.toLanguageKey("effect") == playerStatusEffect.effect.descriptionId
            } }
    }
}