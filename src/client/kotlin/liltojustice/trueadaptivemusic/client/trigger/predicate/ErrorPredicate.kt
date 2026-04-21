package liltojustice.trueadaptivemusic.client.trigger.predicate

import com.google.gson.JsonObject
import liltojustice.trueadaptivemusicapi.trigger.TriggerArguments
import liltojustice.trueadaptivemusicapi.trigger.predicate.state.PredicateState
import liltojustice.trueadaptivemusicapi.trigger.predicate.type.PredicateType

class ErrorPredicate: PredicateType<ErrorPredicate.Arguments, ErrorPredicate.State>(
    "error_predicate") {
    data class Arguments(val actualJson: JsonObject, val reason: String): TriggerArguments()

    override fun validate(arguments: Arguments, state: State): Boolean {
        return false
    }

    override fun create(arguments: Arguments): State {
        return State(arguments)
    }

    class State(val arguments: Arguments): PredicateState() {
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