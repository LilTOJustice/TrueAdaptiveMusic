package liltojustice.trueadaptivemusic.client.serialization.legacy

import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.Constants
import liltojustice.trueadaptivemusic.client.music.tree.RulesParserException
import liltojustice.trueadaptivemusic.client.serialization.legacy.v1.V1MusicTreeJsonConverter
import liltojustice.trueadaptivemusic.client.serialization.legacy.v2.V2MusicTreeJsonConverter

object LegacyMusicTreeJsonConverter {
    fun convert(json: JsonObject, serializationVersion: Int): JsonObject {
        var version = serializationVersion
        var result = json

        while (version < CURRENT_VERSION) {
            result = when (version) {
                1 -> V1MusicTreeJsonConverter.convert(result)
                2 -> V2MusicTreeJsonConverter.convert(result)
                else -> throw RulesParserException("${Constants.RULES_FILENAME} has unknown version tag: $version")
            }

            version++
        }

        return result
    }

    const val CURRENT_VERSION = 3
}