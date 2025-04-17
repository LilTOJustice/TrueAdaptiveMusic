package liltojustice.trueadaptivemusic

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import net.minecraft.util.JsonHelper
import kotlin.io.path.Path

class TrueAdaptiveMusicOptions(
    var selectedPack: String = "",
    var useDebugHud: Boolean = false) {
    private fun toJson(): JsonObject {
        val result = JsonObject()
        result.addProperty("selectedPack", selectedPack)
        result.addProperty("useDebugHud", useDebugHud)

        return result
    }

    fun save() {
        val gson = GsonBuilder().setPrettyPrinting().create()
        Path(Constants.OPTIONS_FILENAME).toFile().writeText(gson.toJson(toJson()))
    }

    companion object {
        fun fromJson(json: JsonObject): TrueAdaptiveMusicOptions {
            return TrueAdaptiveMusicOptions(
                JsonHelper.getString(json, "selectedPack") ?: "",
                JsonHelper.getBoolean(json, "useDebugHud"),
            )
        }
    }
}