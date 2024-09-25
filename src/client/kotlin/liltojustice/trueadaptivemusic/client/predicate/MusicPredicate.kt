package liltojustice.trueadaptivemusic.client.predicate

import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.predicate.custompredicates.StructurePredicate
import net.minecraft.client.MinecraftClient
import net.minecraft.util.JsonHelper

interface MusicPredicate {
    interface MusicPredicateCompanion<TSelf> where TSelf: MusicPredicate {
        fun getTypeName(): String
        fun fromJson(json: JsonObject): TSelf
    }

    class RootPredicate: MusicPredicate {
        override fun test(client: MinecraftClient): Boolean { return true }

        companion object: MusicPredicateCompanion<RootPredicate> {
            override fun getTypeName(): String { return "root" }
            override fun fromJson(json: JsonObject): RootPredicate { return RootPredicate() }
        }
    }

    fun test(client: MinecraftClient): Boolean

    companion object: MusicPredicateCompanion<MusicPredicate> {
        override fun getTypeName(): String { return "" }

        override fun fromJson(json: JsonObject): MusicPredicate {
            return when (val type: String = JsonHelper.getString(json, "type")) {
                RootPredicate.getTypeName() -> RootPredicate.fromJson(json)
                StructurePredicate.getTypeName() -> StructurePredicate.fromJson(json)
                else -> throw Exception("Invalid music predicate type: $type")
            }
        }
    }
}