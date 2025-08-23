package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.MinecraftClient
import net.minecraft.util.JsonHelper
import net.minecraft.world.GameMode

class GameModePredicate(private val gameMode: GameMode): MusicPredicate() {
    override fun test(client: MinecraftClient): Boolean {
        val currentGameMode = client.networkHandler?.getPlayerListEntry(client.player?.uuid ?: return false)?.gameMode

        return currentGameMode == gameMode
    }

    override fun toJson(): JsonObject {
        val result = JsonObject()
        result.addProperty(FIELD_NAME, gameMode.name)

        return result
    }

    companion object: MusicPredicateCompanion<GameModePredicate> {
        override fun fromJson(json: JsonObject): GameModePredicate {
            return GameModePredicate(GameMode.valueOf(JsonHelper.getString(json, FIELD_NAME)))
        }

        private const val FIELD_NAME = "gameMode"
    }
}