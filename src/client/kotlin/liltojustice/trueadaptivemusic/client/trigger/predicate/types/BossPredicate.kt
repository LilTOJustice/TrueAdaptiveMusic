package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusic.client.identifier.EntityTypeIdentifier
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.MinecraftClient
import net.minecraft.text.TranslatableTextContent

class BossPredicate(private val bosses: List<EntityTypeIdentifier>): MusicPredicate() {
    override fun test(): Boolean {
        val client = MinecraftClient.getInstance()
        return client.inGameHud.bossBarHud.bossBars.values.any { bossBar ->
            val bossName = (bossBar.name.content as? TranslatableTextContent)?.key ?: return@any false
            bosses.isEmpty() || bosses.any { boss -> bossName == boss.toTranslationKey("entity") }
        }
    }

    companion object: MusicPredicateCompanion {
        override val argDescriptions: Map<String, String>
            get() = super.argDescriptions + mapOf(
                "bosses" to "List of entities that the music should play for. If none, any entity will trigger the " +
                        "music."
            )
    }
}