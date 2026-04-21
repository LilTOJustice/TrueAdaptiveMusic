package liltojustice.trueadaptivemusic.client.trigger.event

import com.google.gson.JsonObject
import liltojustice.trueadaptivemusicapi.trigger.arguments.TriggerArguments
import liltojustice.trueadaptivemusicapi.trigger.event.type.ClosedEventType
import liltojustice.trueadaptivemusicapi.trigger.state.TriggerState

class ErrorEvent: ClosedEventType<ErrorEvent.Arguments, ErrorEvent.State>(
    "error_event") {
    data class Arguments(val actualJson: JsonObject, val reason: String): TriggerArguments()

    override fun validateEvent(arguments: Arguments, state: State): Boolean {
        return false
    }

    override fun createEventState(arguments: Arguments): State {
        return State(arguments)
    }

    class State(val arguments: Arguments): TriggerState() {
        @Suppress("UNUSED")
        val shortenedJson: JsonObject = run {
            val shortened = arguments.actualJson.deepCopy()
            shortened.remove("musicPath")

            shortened
        }
    }
}