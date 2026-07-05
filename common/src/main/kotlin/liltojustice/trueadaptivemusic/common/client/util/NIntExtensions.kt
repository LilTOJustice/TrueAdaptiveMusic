package liltojustice.trueadaptivemusic.common.client.util

fun String.toNIntOrNull(): NInt? = toUIntOrNull(radix = 10)?.let { NInt(it) }