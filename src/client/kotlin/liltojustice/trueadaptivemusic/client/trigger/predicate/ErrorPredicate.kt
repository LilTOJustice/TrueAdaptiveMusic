package liltojustice.trueadaptivemusic.client.trigger.predicate

import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.Constants
import liltojustice.trueadaptivemusicapi.trigger.arguments.TriggerArguments
import liltojustice.trueadaptivemusicapi.trigger.predicate.type.PredicateType
import liltojustice.trueadaptivemusicapi.trigger.state.TriggerState
import kotlin.reflect.typeOf

object ErrorPredicate: PredicateType<ErrorPredicate.Arguments, ErrorPredicate.State>(
    Constants.ERROR_PREDICATE_NAME, typeOf<Arguments>()
) {
    data class Arguments(val actualJson: JsonObject, val reason: String): TriggerArguments()

    override fun test(arguments: Arguments, state: State): Boolean {
        return false
    }

    override fun createState(arguments: Arguments): State {
        return State(arguments)
    }

    class State(val arguments: Arguments): TriggerState() {
        @Suppress("UNUSED")
        val shortenedJson: JsonObject = run {
            val shortened = arguments.actualJson.deepCopy()
            shortened.remove("musicPath")
            shortened.remove("children")
            shortened.remove("events")
            shortened.remove("parameters")

            shortened
        }
    }
}