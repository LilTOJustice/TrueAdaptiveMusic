package liltojustice.trueadaptivemusic.client.predicate

import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.predicate.custompredicates.*
import net.minecraft.client.MinecraftClient
import net.minecraft.util.JsonHelper
import kotlin.reflect.full.companionObjectInstance

abstract class MusicPredicate(private val partialPath: String) {
    interface MusicPredicateCompanion<TSelf> where TSelf: MusicPredicate {
        fun getTypeName(): String
        fun fromJson(json: JsonObject, partialPath: String): TSelf
    }

    abstract fun test(client: MinecraftClient): Boolean
    abstract fun getIDs(): List<String>
    fun getPredicatePath(): String {
        val companion = javaClass.kotlin.companionObjectInstance
        if (companion is MusicPredicateCompanion<*>)
        {
            return "$partialPath/${companion.getTypeName()}{${getIDs().joinToString(",")}}"
        } else throw MusicPredicateException("Failed to find valid companion object for $javaClass")
    }

    companion object: MusicPredicateCompanion<MusicPredicate> {
        override fun getTypeName(): String {
            throw MusicPredicateException("Attempt to get type name from abstract predicate type.")
        }

        override fun fromJson(json: JsonObject, partialPath: String): MusicPredicate {
            return when (val type: String = JsonHelper.getString(json, "type")) {
                RootPredicate.getTypeName() -> RootPredicate.fromJson(json, partialPath)
                DimensionPredicate.getTypeName() -> DimensionPredicate.fromJson(json, partialPath)
                BiomePredicate.getTypeName() -> BiomePredicate.fromJson(json, partialPath)
                StructurePredicate.getTypeName() -> StructurePredicate.fromJson(json, partialPath)
                CombatPredicate.getTypeName() -> StructurePredicate.fromJson(json, partialPath)
                else -> throw MusicPredicateException("Invalid music predicate type: $type")
            }
        }
    }
}