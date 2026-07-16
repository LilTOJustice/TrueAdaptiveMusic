package liltojustice.trueadaptivemusic.text

import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Component
import net.minecraft.locale.Language

fun translatableWithFallbackOrNull(key: String, fallback: String?): MutableComponent? {
    val language = Language.getInstance()
    if (language.getOrDefault(key) == key) {
        return fallback?.let { Component.literal(fallback) }
    }

    return Component.translatable(key)
}