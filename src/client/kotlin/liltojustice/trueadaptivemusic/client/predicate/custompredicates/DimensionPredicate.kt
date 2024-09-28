package liltojustice.trueadaptivemusic.client.predicate.custompredicates

import com.google.gson.JsonObject
import kotlinx.serialization.json.Json
import liltojustice.trueadaptivemusic.client.predicate.MusicPredicate
import liltojustice.trueadaptivemusic.client.predicate.MusicPredicateException
import net.minecraft.client.MinecraftClient
import net.minecraft.util.Identifier
import net.minecraft.util.JsonHelper

class DimensionPredicate internal constructor(partialPath: String, private val dimension: Identifier) : MusicPredicate(partialPath) {
    override fun test(client: MinecraftClient): Boolean {
        // TODO: Implement this
        return false
    }

    override fun getIDs(): List<String> { return listOf(dimension.toString()) }

    companion object: MusicPredicate.MusicPredicateCompanion<DimensionPredicate> {
        override fun getTypeName(): String { return "dimension" }

        override fun fromJson(json: JsonObject, partialPath: String): DimensionPredicate {
            return DimensionPredicate(partialPath, Identifier(JsonHelper.getString(json, "id")))
        }
    }
}