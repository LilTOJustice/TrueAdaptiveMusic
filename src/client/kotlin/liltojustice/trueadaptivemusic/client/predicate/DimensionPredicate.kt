package liltojustice.trueadaptivemusic.client.predicate

import com.google.gson.JsonObject
import net.minecraft.client.MinecraftClient
import net.minecraft.util.Identifier
import net.minecraft.util.JsonHelper

class DimensionPredicate internal constructor(partialPath: String, private val dimension: Identifier)
    : MusicPredicate(partialPath) {
    override fun test(client: MinecraftClient): Boolean {
        return client.player?.world?.dimensionEntry?.matchesId(dimension) ?: false
    }

    override fun getIDs(): List<String> { return listOf(dimension.toString()) }

    companion object: MusicPredicateCompanion<DimensionPredicate> {
        override fun getTypeName(): String { return "dimension" }

        override fun fromJson(json: JsonObject, partialPath: String): DimensionPredicate {
            return DimensionPredicate(partialPath, Identifier(JsonHelper.getString(json, "id")))
        }
    }
}