package liltojustice.trueadaptivemusic.client.predicate

import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.identifier.EntityTypeIdentifier
import net.minecraft.client.MinecraftClient
import net.minecraft.text.TranslatableTextContent
import net.minecraft.util.JsonHelper

class BossPredicate(private val boss: EntityTypeIdentifier): MusicPredicate() {
    override fun test(client: MinecraftClient): Boolean {
        return client.inGameHud.bossBarHud.bossBars.values.any { bossBar ->
            toTranslationKey(
                (bossBar.name.content as? TranslatableTextContent)?.key ?: return@any false) ==
                    boss.toTranslationKey()
        }
    }

    override fun getIDs(): List<String> { return listOf(boss.toString()) }

    companion object: MusicPredicateCompanion<BossPredicate> {
        override fun getTypeName(): String { return "boss" }

        override fun fromJson(json: JsonObject): BossPredicate {
            return BossPredicate(EntityTypeIdentifier(JsonHelper.getString(json, "id")))
        }

        fun toTranslationKey(textKey: String): String {
            return textKey.split(".").drop(1).joinToString(".")
        }
    }
}