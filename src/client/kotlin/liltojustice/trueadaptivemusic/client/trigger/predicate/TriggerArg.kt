package liltojustice.trueadaptivemusic.client.trigger.predicate

data class TriggerArg(val name: String, val value: Any?) {
    override fun toString(): String {
        val valueText: String =
                if (value is Iterable<*>)
                    "[${value.joinToString(",")}]"
                else
                    value.toString()
        return "$name($valueText)"
    }
}