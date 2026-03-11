package liltojustice.trueadaptivemusic.client.music.pack

import com.google.gson.GsonBuilder
import liltojustice.trueadaptivemusic.ReflectionHelper
import liltojustice.trueadaptivemusic.text.StringExtensions.prettify
import liltojustice.trueadaptivemusic.text.translatableWithFallbackOrNull
import net.minecraft.text.Text
import kotlin.reflect.KParameter
import kotlin.reflect.full.primaryConstructor

data class MusicPackOptions(val description: String = "", val persistentNodeMusic: Boolean = false) {
    fun getArgs(): List<Any?> {
        return ReflectionHelper.getConstructorParameterValues(this).map { param -> param.value }
    }

    fun jsonEncode(): String {
        return json.toJson(this)
    }

    companion object {
        private val displayNames = MusicPackOptions::class
            .primaryConstructor
            ?.parameters
            ?.mapNotNull { it.name }
            ?.associateWith { it.prettify() } ?: mapOf()

        private val descriptions = mapOf(
            "description" to "Description of the Music Pack.",
            "persistentNodeMusic" to "If checked, music from the current node will continue to play until it" +
                    " finishes if another node is chosen. Disables music fading between nodes."
        )

        private val json = GsonBuilder()
            .setPrettyPrinting()
            .create()

        fun jsonDecode(string: String): MusicPackOptions {
            return json.fromJson(string, MusicPackOptions::class.java)
        }

        fun getRequiredArgs(): List<KParameter> {
            return MusicPackOptions::class.primaryConstructor?.parameters ?: emptyList()
        }

        fun getArgDisplayName(argName: String): Text? {
            return translatableWithFallbackOrNull(
                "trueadaptivemusic.pack_options.${argName}.display", displayNames[argName])
        }

        fun getArgDescription(argName: String): Text? {
            return translatableWithFallbackOrNull(
                "trueadaptivemusic.pack_options.${argName}.description", descriptions[argName])
        }
    }
}
