package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.MinecraftClient
import net.minecraft.util.JsonHelper
import net.minecraft.world.GameMode

class GameModePredicate(private val gameMode: GameMode): MusicPredicate() {
    override fun test(): Boolean {
        val client = MinecraftClient.getInstance()
        val currentGameMode = client.networkHandler?.getPlayerListEntry(client.player?.uuid ?: return false)?.gameMode

        return currentGameMode == gameMode
    }

    override fun toJson(): JsonObject {
        val result = JsonObject()
        result.addProperty(FIELD_NAME, gameMode.name)

        return result
    }

    companion object: MusicPredicateCompanion<GameModePredicate> {
        override val argDescriptions: Map<String, String>
            get() = super.argDescriptions + mapOf(
                "gameMode" to "Which game mode to be in for the music to play."
            )

        override fun fromJson(json: JsonObject): GameModePredicate {
            return GameModePredicate(GameMode.valueOf(JsonHelper.getString(json, FIELD_NAME)))
        }

        private const val FIELD_NAME = "gameMode"
    }
}