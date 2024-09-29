package liltojustice.trueadaptivemusic.client.predicate.custompredicates

import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.predicate.MusicPredicate
import net.minecraft.client.MinecraftClient
import net.minecraft.util.Identifier
import net.minecraft.util.JsonHelper

class CombatPredicate internal constructor(partialPath:String, private val mob: Identifier)
    : MusicPredicate(partialPath) {
    override fun test(client: MinecraftClient): Boolean {
        // TODO: Implement this
        return false
    }

    override fun getIDs(): List<String> { return listOf(mob.toString()) }

    companion object: MusicPredicateCompanion<CombatPredicate> {
        override fun getTypeName(): String { return "combat" }

        override fun fromJson(json: JsonObject, partialPath: String): CombatPredicate {
            return CombatPredicate(partialPath, Identifier(JsonHelper.getString(json, "id")))
        }
    }
}