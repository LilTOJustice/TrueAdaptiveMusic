package liltojustice.trueadaptivemusic.client.util

import kotlin.random.Random

class WeightedList<T>(weights: Map<T, PDouble> = emptyMap(), val defaultWeight: PDouble = PDouble(1.0)) {
    val weights = weights.withDefault { defaultWeight }
    val total = weights.values.sumOf { it.toDouble() }

    fun filter(keySet: Set<T>, union: Boolean): WeightedList<T> {
        return if (union) {
            val newWeights = weights.toMutableMap()
            keySet.forEach { newWeights.putIfAbsent(it, defaultWeight) }

            WeightedList(newWeights)
        }
        else {
           filter { keySet.contains(it) }
        }
    }

    fun filter(filter: (T) -> Boolean): WeightedList<T> {
        return WeightedList(weights.filter { kv -> filter(kv.key) }, defaultWeight)
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