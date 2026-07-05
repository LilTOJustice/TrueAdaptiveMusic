package liltojustice.trueadaptivemusic.common.text

import net.minecraft.locale.Language
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent

fun translatableWithFallbackOrNull(key: String, fallback: String?): MutableComponent? {
    val language = Language.getInstance()
    if (language.getOrDefault(key) == key) {
        return fallback?.let { Component.literal(fallback) }
    }

    return Component.translatable(key)
}