package liltojustice.trueadaptivemusic.client.event.types

import com.google.gson.JsonObject

class OnRecipeUnlockEvent(): MusicEvent() {
    companion object: MusicEventCompanion<OnRecipeUnlockEvent> {
        override fun getTypeName(): String {
            return "on_recipe_unlock"
        }

        override fun fromJson(json: JsonObject): MusicEvent {
            return OnRecipeUnlockEvent()
        }
    }
}