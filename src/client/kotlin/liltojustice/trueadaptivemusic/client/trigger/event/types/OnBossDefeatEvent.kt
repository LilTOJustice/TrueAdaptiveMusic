package liltojustice.trueadaptivemusic.client.trigger.event.types

import liltojustice.trueadaptivemusic.client.trigger.event.MusicEvent
import liltojustice.trueadaptivemusic.client.identifier.EntityTypeIdentifier
import net.minecraft.util.Identifier

class OnBossDefeatEvent(private val bosses: List<EntityTypeIdentifier>): MusicEvent() {
    override fun validate(vararg eventArgs: Any?): Boolean {
        val bossId = Identifier.tryParse((eventArgs[0] as? EntityTypeIdentifier)
            ?.path?.split(".")?.drop(1)?.joinToString(":")) ?: return false
        return bosses.isEmpty()
                || bosses.any {
                    bossId.namespace == it.namespace && bossId.path.split(".").lastOrNull() == it.path }
    }

    companion object: MusicEventCompanion {
        override val argDescriptions: Map<String, String>
            get() = super.argDescriptions + mapOf(
                "bosses" to "Which entities the music should play for when their boss bar hits zero."
            )
    }
}