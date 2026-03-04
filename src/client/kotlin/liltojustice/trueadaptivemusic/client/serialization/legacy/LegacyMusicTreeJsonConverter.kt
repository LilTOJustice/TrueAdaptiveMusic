package liltojustice.trueadaptivemusic.client.serialization.legacy

import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.Constants
import liltojustice.trueadaptivemusic.client.music.tree.RulesParserException
import liltojustice.trueadaptivemusic.client.serialization.legacy.original.OriginalMusicTreeJsonConverter

object LegacyMusicTreeJsonConverter {
    fun convert(json: JsonObject, serializationVersion: Int?): JsonObject {
        return when (serializationVersion) {
            null -> OriginalMusicTreeJsonConverter.convert(json)
            else -> throw RulesParserException(
                "${Constants.Companion.RULES_FILENAME} has unknown version tag: $serializationVersion")
        }
    }
}