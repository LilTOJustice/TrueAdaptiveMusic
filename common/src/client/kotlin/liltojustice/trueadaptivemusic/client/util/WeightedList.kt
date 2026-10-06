package liltojustice.trueadaptivemusic.client.util

import kotlin.random.Random

class WeightedList<T>(val weights: Map<T, PDouble> = emptyMap()) {
    val total = weights.values.sumOf { it.toDouble() }

    fun filter(keySet: Set<T>): WeightedList<T> {
        return filter { keySet.contains(it) }
    }

    fun filter(filter: (T) -> Boolean): WeightedList<T> {
        return WeightedList(weights.filter { kv -> filter(kv.key) })
    }

    fun getWeightedRandomOrNull(): T? {
        if (weights.isEmpty()) {
            return null
        }

        var remaining = Random.nextDouble(0.0, total)
        val weightList = weights.toList()
        for (entry in weightList) {
            remaining -= entry.second.toDouble()
            if (remaining <= 0) {
                return entry.first
            }
        }

        return weightList.last().first
    }

    operator fun get(value: T): PDouble? {
        return weights[value]
    }
}