package liltojustice.trueadaptivemusic.client.trigger.event.types

import liltojustice.trueadaptivemusic.client.identifier.EntityTypeIdentifier
import liltojustice.trueadaptivemusicapi.trigger.arguments.TriggerArguments
import liltojustice.trueadaptivemusicapi.trigger.event.input.EventInput
import liltojustice.trueadaptivemusicapi.trigger.event.type.StaticEventType
import net.minecraft.resources.Identifier
import kotlin.reflect.typeOf

object OnBossDefeatEvent : StaticEventType<OnBossDefeatEvent.Arguments, OnBossDefeatEvent.Input>(
    "on_boss_defeat", typeOf<Arguments>()
) {
    override val argDescriptions: Map<String, String>
        get() = super.argDescriptions + mapOf(
            Arguments::bosses.name to "Which entities the music should play for when their boss bar hits zero.")

    data class Arguments(val bosses: List<EntityTypeIdentifier>): TriggerArguments()

    data class Input(val boss: EntityTypeIdentifier): EventInput()

    override fun validate(arguments: Arguments, input: Input): Boolean {
        val bossId = input.boss
            .path
            .split(".")
            .drop(1)
            .joinToString(":")
            .let { Identifier.tryParse(it) }
            ?: return false

        return arguments.bosses.isEmpty()
                || arguments.bosses.any {
                    bossId.namespace == it.namespace && bossId.path.split(".").lastOrNull() == it.path }
    }
}