package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.identifier.EntityTypeIdentifier
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.MinecraftClient
import net.minecraft.text.TranslatableTextContent
import net.minecraft.util.JsonHelper

class BossPredicate(private val bosses: List<EntityTypeIdentifier>): MusicPredicate() {
    override fun test(client: MinecraftClient): Boolean {
        return client.inGameHud.bossBarHud.bossBars.values.any { bossBar ->
            val bossName = (bossBar.name.content as? TranslatableTextContent)?.key ?: return@any false
            bosses.isEmpty() || bosses.any { boss -> bossName == boss.toTranslationKey("entity") }
        }
    }

    override fun toJson(): JsonObject {
        val result = super.toJson()
        val jsonBosses = JsonArray()
        bosses.forEach { boss -> jsonBosses.add(boss.toString()) }
        result.add("id", jsonBosses)

        return result
    }

    companion object: MusicPredicateCompanion<BossPredicate> {
        override fun getTypeName(): String { return "boss" }

        override fun fromJson(json: JsonObject): BossPredicate {
            return BossPredicate(
                if (JsonHelper.hasArray(json, "id"))
                    JsonHelper.getArray(json, "id").map { element -> EntityTypeIdentifier(element.asString) }
                else
                    listOf(EntityTypeIdentifier(JsonHelper.getString(json, "id"))))
        }
    }
}