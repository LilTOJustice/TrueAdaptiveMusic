package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusic.client.identifier.EntityTypeIdentifier
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.contents.TranslatableContents

class BossPredicate(private val bosses: List<EntityTypeIdentifier>): MusicPredicate() {
    override fun test(): Boolean {
        return Minecraft.getInstance().gui.bossOverlay.events.values.any { bossBar ->
            val bossName = (bossBar.name.contents as? TranslatableContents)?.key ?: return@any false
            bosses.isEmpty() || bosses.any { boss -> bossName == boss.toLanguageKey("entity") }
        }
    }

    companion object: MusicPredicateCompanion {
        override val argDescriptions: Map<String, String>
            get() = super.argDescriptions + mapOf(
                BossPredicate::bosses.name to "List of entities that the music should play for. If none, any entity " +
                        "will trigger the music."
            )
    }
}