package liltojustice.trueadaptivemusic.common.client.trigger.event

import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.common.Constants
import liltojustice.trueadaptivemusicapi.trigger.arguments.TriggerArguments
import liltojustice.trueadaptivemusicapi.trigger.event.type.ClosedEventType
import liltojustice.trueadaptivemusicapi.trigger.state.TriggerState
import kotlin.reflect.typeOf

object ErrorEvent: ClosedEventType<ErrorEvent.Arguments, ErrorEvent.State>(
    Constants.ERROR_EVENT_NAME, typeOf<Arguments>()
) {
    data class Arguments(val actualJson: JsonObject, val reason: String): TriggerArguments()

    override fun validate(arguments: Arguments, state: State): Boolean {
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

            shortened
        }
    }
}