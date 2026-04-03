package liltojustice.trueadaptivemusic.client.trigger.event.types

import liltojustice.trueadaptivemusic.client.trigger.event.MusicEvent
import liltojustice.trueadaptivemusic.client.identifier.EntityTypeIdentifier
import net.minecraft.resources.Identifier

class OnBossDefeatEvent(private val bosses: List<EntityTypeIdentifier>): MusicEvent() {
    override fun validate(vararg eventArgs: Any?): Boolean {
        val bossId = (eventArgs[0] as? EntityTypeIdentifier)
            ?.path
            ?.split(".")
            ?.drop(1)
            ?.joinToString(":")
            ?.let { Identifier.tryParse(it) }
            ?: return false

        return bosses.isEmpty()
                || bosses.any {
                    bossId.namespace == it.namespace && bossId.path.split(".").lastOrNull() == it.path }
    }

    companion object: MusicEventCompanion {
        override val argDescriptions: Map<String, String>
            get() = super.argDescriptions + mapOf(
                OnBossDefeatEvent::bosses.name to "Which entities the music should play for when their boss bar " +
                        "hits zero."
            )
    }
}