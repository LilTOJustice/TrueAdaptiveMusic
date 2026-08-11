package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusic.client.util.TimeOfDay
import liltojustice.trueadaptivemusicapi.trigger.arguments.TriggerArguments
import liltojustice.trueadaptivemusicapi.trigger.predicate.type.StaticPredicateType
import net.minecraft.client.Minecraft
import kotlin.reflect.typeOf

object TimeOfDayPredicate: StaticPredicateType<TimeOfDayPredicate.Arguments>(
    "time_of_day",
    typeOf<Arguments>()
) {
    override val argDescriptions: Map<String, String>
        get() = super.argDescriptions + mapOf(
            Arguments::start.name to "Start time in ticks (0 to 23999) for the time range for music to play. Should " +
                    "be less than \"end\".",
            Arguments::end.name to "End time in ticks (0 to 23999) for the time range for music to play. Should be " +
                    "greater than \"start\"."
        )

    data class Arguments(val start: TimeOfDay, val end: TimeOfDay): TriggerArguments()

    override fun test(arguments: Arguments): Boolean {
        val level = Minecraft.getInstance().level ?: return false
        val time = level.dayTime % 24000

        return time >= arguments.start.value.toLong() && time <= arguments.end.value.toLong()
    }
}