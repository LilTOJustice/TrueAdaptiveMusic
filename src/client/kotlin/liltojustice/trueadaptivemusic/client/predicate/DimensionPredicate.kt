package liltojustice.trueadaptivemusic.client.predicate

import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.identifier.DimensionIdentifier
import net.minecraft.client.MinecraftClient
import net.minecraft.util.JsonHelper

class DimensionPredicate(private val dimension: DimensionIdentifier): MusicPredicate() {
    override fun test(client: MinecraftClient): Boolean {
        return client.player?.world?.dimensionEntry?.matchesId(dimension) ?: false
    }

    override fun getIDs(): List<String> { return listOf(dimension.toString()) }

    companion object: MusicPredicateCompanion<DimensionPredicate> {
        override fun getTypeName(): String { return "dimension" }

        override fun fromJson(json: JsonObject): DimensionPredicate {
            return DimensionPredicate(DimensionIdentifier(JsonHelper.getString(json, "id")))
        }
    }
}