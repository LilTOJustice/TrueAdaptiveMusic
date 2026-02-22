package liltojustice.trueadaptivemusic.client

import com.google.gson.GsonBuilder
import liltojustice.trueadaptivemusic.Constants
import liltojustice.trueadaptivemusic.ReflectionHelper
import liltojustice.trueadaptivemusic.text.StringExtensions.prettify
import liltojustice.trueadaptivemusic.text.translatableWithFallbackOrNull
import net.minecraft.text.Text
import kotlin.reflect.KParameter
import kotlin.reflect.full.primaryConstructor

data class TrueAdaptiveMusicOptions(
    val selectedPack: String = "",
    val useDebugHud: Boolean = false,
    val prettifyIdentifiers: Boolean = true,
    val musicLoudnessBoost: LUFBoost = LUFBoost(0U),
    val ambienceLoudnessBoost: LUFBoost = LUFBoost(0U)
) {
    fun save() {
        Constants.Companion.OPTIONS_PATH.toFile().writeText(jsonEncode())
    }

    fun getArgs(): List<Any?> {
        return ReflectionHelper.Companion.getConstructorParameterValues(this).map { param -> param.value }
    }

    private fun jsonEncode(): String {
        return json.toJson(this)
    }

    companion object {
        private val displayNames = TrueAdaptiveMusicOptions::class
            .primaryConstructor
            ?.parameters
            ?.mapNotNull { it.name }
            ?.associateWith { it.prettify() } ?: mapOf()

        private val descriptions = mapOf(
            "useDebugHud" to "Enable or disable the True Adaptive Music debug hud. Good for when creating a music " +
                    "pack.",
            "prettifyIdentifiers" to "Enable or disable \"prettified\" identifiers (makes them more human friendly).",
            "musicLoudnessBoost" to "Increase the music volume by passing a higher LUFS value to FFmpeg. " +
                    "Requires FFmpeg.",
            "ambienceLoudnessBoost" to "Increase the ambience volume by passing a higher LUFS value to FFmpeg. " +
                    "Requires FFmpeg."
        )

        private val json = GsonBuilder()
            .setPrettyPrinting()
            .create()

        fun jsonDecode(string: String): TrueAdaptiveMusicOptions {
            return json.fromJson(string, TrueAdaptiveMusicOptions::class.java)
        }

        fun getRequiredArgs(): List<KParameter> {
            return TrueAdaptiveMusicOptions::class.primaryConstructor?.parameters?.drop(1) ?: emptyList()
        }

        fun getArgDisplayName(argName: String): Text? {
            return translatableWithFallbackOrNull(
                "trueadaptivemusic.options.${argName}.display", displayNames[argName])
        }

        fun getArgDescription(argName: String): Text? {
            return translatableWithFallbackOrNull(
                "trueadaptivemusic.options.${argName}.description", descriptions[argName])
        }
    }

    class LUFBoost(val value: UInt) {
        companion object {
            const val MAX_VALUE = 10U
        }
    }
}