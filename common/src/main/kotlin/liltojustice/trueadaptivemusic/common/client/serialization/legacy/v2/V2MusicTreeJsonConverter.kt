package liltojustice.trueadaptivemusic.common.client.serialization.legacy.v2

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.common.client.music.tree.MusicTree
import liltojustice.trueadaptivemusic.common.client.serialization.legacy.v2.model.trigger.event.MusicEvent
import liltojustice.trueadaptivemusic.common.client.serialization.legacy.v2.model.trigger.predicate.MusicPredicate

object V2MusicTreeJsonConverter {
    private const val TARGET_VERSION = 3

    fun convert(json: JsonObject): JsonObject {
        val result = JsonObject()
        result.addProperty("version", TARGET_VERSION)
        result.add("root", convertNode(json.getAsJsonObject("root")))

        return result
    }

    fun convertNode(json: JsonObject): JsonObject {
        val result = JsonObject()

        result.add("music", json.getAsJsonArray("music") ?: JsonArray())
        result.add("ambience", json.getAsJsonArray("ambience") ?: JsonArray())

        val predicates = JsonArray()
        json.getAsJsonArray("predicates")?.forEach { element ->
            predicates.add(MusicPredicate.convert(element))
        }
        result.add("predicates", predicates)

        val events = JsonArray()
        json.getAsJsonArray("events")?.forEach { element ->
            events.add(MusicEvent.convert(element)) }
        result.add("events", events)

        result.add(
            "parameters",
            json.get("parameters") ?: Gson().toJsonTree(MusicTree.Node.Parameters.default())
        )

        val children = JsonArray()
        json
            .getAsJsonArray("children")
            ?.forEach { element -> children.add(convertNode(element.asJsonObject)) }
        result.add("children", children)

        return result
    }
}