package liltojustice.trueadaptivemusic.text

import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Language

fun translatableWithFallbackOrNull(key: String, fallback: String?): MutableText? {
    val language = Language.getInstance()
    if (language.get(key) == key) {
        return fallback?.let { Text.literal(fallback) }
    }

    return Text.translatable(key)
}