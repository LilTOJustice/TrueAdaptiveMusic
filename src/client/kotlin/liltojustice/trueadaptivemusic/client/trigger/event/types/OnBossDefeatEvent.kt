package liltojustice.trueadaptivemusic.client.trigger.event.types

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.trigger.event.MusicEvent
import liltojustice.trueadaptivemusic.client.identifier.EntityTypeIdentifier
import net.minecraft.util.Identifier
import net.minecraft.util.JsonHelper

class OnBossDefeatEvent(private val bosses: List<EntityTypeIdentifier>): MusicEvent() {
    override fun validate(vararg eventArgs: Any?): Boolean {
        val bossId = Identifier.tryParse((eventArgs[0] as? Identifier)
            ?.path?.split(".")?.drop(1)?.joinToString(":")) ?: return false
        return bosses.isEmpty()
                || bosses.any {
                    bossId.namespace == it.namespace && bossId.path.split(".").lastOrNull() == it.path }
    }

    override fun toJson(): JsonObject {
        val result = JsonObject()
        val bossesArray = JsonArray()
        bosses.forEach { bossesArray.add(it.toString()) }
        result.add("bosses", bossesArray)

        return result
    }

    companion object: MusicEventCompanion<OnBossDefeatEvent> {
        override fun fromJson(json: JsonObject): MusicEvent {
            return OnBossDefeatEvent(
                JsonHelper.getArray(json, "bosses").map { element -> EntityTypeIdentifier(element.asString) })
        }
    }
}