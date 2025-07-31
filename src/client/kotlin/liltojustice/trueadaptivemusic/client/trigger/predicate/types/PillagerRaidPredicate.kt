package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.MinecraftClient

class PillagerRaidPredicate(): MusicPredicate() {
    override fun test(client: MinecraftClient): Boolean {
        val clientWorld = client.world ?: return false
        val serverWorld = client.server?.worlds?.firstOrNull { world -> world.registryKey == clientWorld.registryKey }
            ?: return false

        return serverWorld.hasRaidAt(client.player?.blockPos ?: return false)
    }

    companion object: MusicPredicateCompanion<PillagerRaidPredicate> {
        override fun getTypeName(): String { return "pillager_raid"}

        override fun fromJson(json: JsonObject): PillagerRaidPredicate {
            return PillagerRaidPredicate()
        }
    }
}