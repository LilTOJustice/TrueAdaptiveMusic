package liltojustice.trueadaptivemusic

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.minecraft.text.Text
import kotlin.reflect.KParameter
import kotlin.reflect.full.primaryConstructor

@Serializable
data class TrueAdaptiveMusicOptions(
    val selectedPack: String = "",
    val useDebugHud: Boolean = false) {

    fun save() {
        Constants.OPTIONS_PATH.toFile().writeText(jsonEncode())
    }

    fun getArgs(): List<Any?> {
        return ReflectionHelper.getConstructorParameterValues(this).map { param -> param.value }
    }

    private fun jsonEncode(): String {
        return json.encodeToString(this)
    }

    companion object {
        private val descriptions = mapOf(
            "useDebugHud" to "Enable or disable the True Adaptive Music debug hud. Good for when creating a music pack."
        )

        private val json = Json {
            encodeDefaults = true
            prettyPrint = true
            ignoreUnknownKeys = true
        }

        fun jsonDecode(string: String): TrueAdaptiveMusicOptions {
            return json.decodeFromString(string)
        }

        fun getRequiredArgs(): List<KParameter> {
            return TrueAdaptiveMusicOptions::class.primaryConstructor?.parameters ?: emptyList()
        }

        fun getArgDescription(argName: String): Text {
            return Text.translatableWithFallback(
                "trueadaptivemusic:options_${argName}_description", descriptions[argName])
        }
    }
}