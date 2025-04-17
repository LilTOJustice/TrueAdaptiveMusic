package liltojustice.trueadaptivemusic

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.io.path.Path
import kotlin.reflect.KParameter
import kotlin.reflect.full.primaryConstructor

@Serializable
data class TrueAdaptiveMusicOptions(
    val selectedPack: String = "",
    val useDebugHud: Boolean = false) {

    fun save() {
        Path(Constants.OPTIONS_FILENAME).toFile().writeText(jsonEncode())
    }

    fun getArgs(): List<Any?> {
        return ReflectionHelper.getConstructorParameterValues(this).map { param -> param.value }
    }

    private fun jsonEncode(): String {
        return json.encodeToString(this)
    }

    companion object {
        private val json = Json {
            encodeDefaults = true
            prettyPrint = true
        }

        fun jsonDecode(string: String): TrueAdaptiveMusicOptions {
            return json.decodeFromString(string)
        }

        fun getRequiredArgs(): List<KParameter> {
            return TrueAdaptiveMusicOptions::class.primaryConstructor?.parameters ?: emptyList()
        }
    }
}