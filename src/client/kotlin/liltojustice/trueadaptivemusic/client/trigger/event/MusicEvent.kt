package liltojustice.trueadaptivemusic.client.trigger.event

import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.InvokeMusicEventCallback
import liltojustice.trueadaptivemusic.client.TAMClient
import liltojustice.trueadaptivemusic.client.trigger.MusicTrigger

abstract class MusicEvent: MusicTrigger() {
    var isPersistent = false

    open fun validate(vararg eventArgs: Any?): Boolean {
        return true
    }

    override fun toJson(): JsonObject {
        val result = super.toJson()
        result.addProperty("persistent", isPersistent)

        return result
    }

    final override fun initParams(json: JsonObject) {
        if (json.has("persistent")) {
            isPersistent = json.getAsJsonPrimitive("persistent").asBoolean
        }
    }

    final override fun getTypeName(): String {
        return if (this is ErrorEvent)
            ErrorEvent.NAME
        else
            TAMClient.eventRegistry[this::class]
    }

    companion object: MusicEventCompanion<MusicEvent> {
    }

    interface MusicEventCompanion<TSelf>: MusicTriggerCompanion<MusicEvent> where TSelf: MusicEvent {
        fun invokeMusicEvent(eventName: String, vararg eventArgs: Any?) {
            InvokeMusicEventCallback.EVENT.invoker().invokeMusicEvent(eventName, *eventArgs)
        }
    }
}