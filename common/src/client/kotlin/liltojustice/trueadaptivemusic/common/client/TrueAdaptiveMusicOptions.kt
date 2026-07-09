package liltojustice.trueadaptivemusic.common.client

import com.google.gson.GsonBuilder
import liltojustice.trueadaptivemusic.common.Constants
import liltojustice.trueadaptivemusic.common.ReflectionHelper
import liltojustice.trueadaptivemusic.common.text.StringExtensions.prettify
import liltojustice.trueadaptivemusic.common.text.translatableWithFallbackOrNull
import net.minecraft.network.chat.Component
import kotlin.reflect.KParameter
import kotlin.reflect.full.primaryConstructor

data class TrueAdaptiveMusicOptions(
    val selectedPack: String = "",
    val useDebugHud: Boolean = false,
    val prettifyIdentifiers: Boolean = true,
    val audioNormalization: Boolean = true,
    val musicLoudnessBoost: LUFBoost = LUFBoost(0U),
    val ambienceLoudnessBoost: LUFBoost = LUFBoost(0U)
) {
    fun save() {
        Constants.OPTIONS_PATH.toFile()
        Constants.OPTIONS_PATH.toFile().writeText(jsonEncode())
    }

    fun getArgs(): List<Any?> {
        return ReflectionHelper.getConstructorParameterValues(this).map { param -> param.value }
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
            TrueAdaptiveMusicOptions::useDebugHud.name to "Enable or disable the True Adaptive Music debug hud. Good " +
                    "for when creating a music pack.",
            TrueAdaptiveMusicOptions::prettifyIdentifiers.name to "Enable or disable \"prettified\" identifiers " +
                    "(makes them more human friendly).",
            TrueAdaptiveMusicOptions::audioNormalization.name to "Enable or disable audio normalization. This will " +
                    "make all tracks be similar volume levels, and allow usage of the loudness boost options.",
            TrueAdaptiveMusicOptions::musicLoudnessBoost.name to "Increase the music volume by passing a higher " +
                    "LUFS value to FFmpeg. Requires FFmpeg.",
            TrueAdaptiveMusicOptions::ambienceLoudnessBoost.name to "Increase the ambience volume by passing a " +
                    "higher LUFS value to FFmpeg. Requires FFmpeg."
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

        fun getArgDisplayName(argName: String): Component? {
            return translatableWithFallbackOrNull(
                "trueadaptivemusic.options.${argName}.display", displayNames[argName])
        }

        fun getArgDescription(argName: String): Component? {
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