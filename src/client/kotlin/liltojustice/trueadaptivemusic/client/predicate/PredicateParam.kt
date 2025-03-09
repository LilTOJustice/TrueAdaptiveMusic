package liltojustice.trueadaptivemusic.client.predicate

data class PredicateParam(val name: String, val value: Any?) {
    override fun toString(): String {
        return "$name($value)"
    }
}