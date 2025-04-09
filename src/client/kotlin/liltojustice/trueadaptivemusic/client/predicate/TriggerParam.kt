package liltojustice.trueadaptivemusic.client.predicate

data class TriggerParam(val name: String, val value: Any?) {
    override fun toString(): String {
        val valueText: String =
                if (value is Iterable<*>)
                    "[${value.joinToString(",")}]"
                else
                    value.toString()
        return "$name($valueText)"
    }
}