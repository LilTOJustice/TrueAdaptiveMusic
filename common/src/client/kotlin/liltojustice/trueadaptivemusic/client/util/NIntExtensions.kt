package liltojustice.trueadaptivemusic.client.util

fun String.toNIntOrNull(): NInt? = toUIntOrNull(radix = 10)?.let { NInt(it) }