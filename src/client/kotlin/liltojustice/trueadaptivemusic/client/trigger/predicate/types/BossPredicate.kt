package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusic.client.identifier.EntityTypeIdentifier
import liltojustice.trueadaptivemusicapi.trigger.arguments.TriggerArguments
import liltojustice.trueadaptivemusicapi.trigger.predicate.type.StaticPredicateType
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.contents.TranslatableContents
import kotlin.reflect.typeOf

object BossPredicate: StaticPredicateType<BossPredicate.Arguments>(
    "boss", typeOf<Arguments>()
) {
    override val argDescriptions: Map<String, String>
        get() = super.argDescriptions + mapOf(
            Arguments::bosses.name to "List of entities that the music should play for. If none, any entity will " +
                    "trigger the music."
        )

    override fun test(arguments: Arguments): Boolean {
        return Minecraft.getInstance().gui.bossOverlay.events.values.any { bossBar ->
            val bossName = (bossBar.name.contents as? TranslatableContents)?.key ?: return@any false
            arguments.bosses.isEmpty() ||
                    arguments.bosses.any { boss -> bossName == boss.toLanguageKey("entity") }
        }
    }

    data class Arguments(val bosses: List<EntityTypeIdentifier>): TriggerArguments()
}