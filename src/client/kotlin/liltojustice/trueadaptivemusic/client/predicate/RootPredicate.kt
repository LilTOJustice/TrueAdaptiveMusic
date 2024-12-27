package liltojustice.trueadaptivemusic.client.predicate

import com.google.gson.JsonObject
import net.minecraft.client.MinecraftClient

class RootPredicate(partialPath: String): MusicPredicate(partialPath) {
    override fun test(client: MinecraftClient): Boolean { return true }
    override fun getIDs(): List<String> { return listOf() }

    companion object: MusicPredicateCompanion<RootPredicate> {
        override fun getTypeName(): String { return "root" }

        override fun fromJson(json: JsonObject, partialPath: String): RootPredicate { return RootPredicate(partialPath) }
    }
}
