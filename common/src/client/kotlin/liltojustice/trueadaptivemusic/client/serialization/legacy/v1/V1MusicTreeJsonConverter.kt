package liltojustice.trueadaptivemusic.client.serialization.legacy.v1

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.music.tree.MusicTreeNode
import liltojustice.trueadaptivemusic.client.serialization.legacy.v1.model.trigger.event.MusicEvent
import liltojustice.trueadaptivemusic.client.serialization.legacy.v1.model.trigger.predicate.MusicPredicate

object V1MusicTreeJsonConverter {
    private const val TARGET_VERSION = 2

    fun convert(json: JsonObject): JsonObject {
        val result = JsonObject()
        result.addProperty("version", TARGET_VERSION)
        result.add("root", convertPredicateToNode(json))

        return result
    }

    fun convertPredicateToNode(json: JsonObject): JsonObject {
        val result = JsonObject()

        result.add("music", json.getAsJsonArray("musicPath") ?: JsonArray())
        result.add("ambience", json.getAsJsonArray("ambiencePath") ?: JsonArray())

        val predicates = JsonArray()
        predicates.add(MusicPredicate.convert(json))
        result.add("predicates", predicates)

        val events = JsonArray()
        json.getAsJsonArray("events")?.forEach { element -> events.add(MusicEvent.convert(element)) }
        result.add("events", events)

        result.add(
            "parameters",
            json.get("parameters") ?: Gson().toJsonTree(MusicTreeNode.Parameters.default())
        )

        val children = JsonArray()
        json
            .getAsJsonArray("children")
            ?.forEach { element -> children.add(convertPredicateToNode(element.asJsonObject)) }
        result.add("children", children)

        return result
    }
}