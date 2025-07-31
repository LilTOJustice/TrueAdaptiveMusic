package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.identifier.StatusEffectIdentifier
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.MinecraftClient
import net.minecraft.util.JsonHelper

class StatusEffectPredicate(private val statusEffects: List<StatusEffectIdentifier>): MusicPredicate() {
    override fun test(client: MinecraftClient): Boolean {
        val playerStatusEffects = client.player?.statusEffects ?: return false

        return statusEffects.any { statusEffect ->
            playerStatusEffects.any { playerStatusEffect ->
                statusEffect.toTranslationKey("effect") == playerStatusEffect.effectType.translationKey } }
    }

    override fun toJson(): JsonObject {
        val result = super.toJson()
        val jsonEntities = JsonArray()
        statusEffects.forEach { statusEffect -> jsonEntities.add(statusEffect.toString()) }
        result.add("statusEffects", jsonEntities)

        return result
    }

    companion object: MusicPredicateCompanion<StatusEffectPredicate> {
        override fun getTypeName(): String { return "status_effect" }

        override fun fromJson(json: JsonObject): StatusEffectPredicate {
            return StatusEffectPredicate(
                JsonHelper.getArray(json, "statusEffects")
                    .map { element -> StatusEffectIdentifier(element.asString) })
        }
    }
}