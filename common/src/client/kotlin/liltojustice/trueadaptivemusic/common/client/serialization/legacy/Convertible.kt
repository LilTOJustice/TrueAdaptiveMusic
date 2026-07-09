package liltojustice.trueadaptivemusic.common.client.serialization.legacy

import com.google.gson.JsonElement
import com.google.gson.JsonObject

interface Convertible {
    fun convert(json: JsonElement): JsonObject
}