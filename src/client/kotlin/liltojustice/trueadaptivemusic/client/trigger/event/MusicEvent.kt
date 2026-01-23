package liltojustice.trueadaptivemusic.client.trigger.event

import com.google.gson.Gson
import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.InvokeMusicEventCallback
import liltojustice.trueadaptivemusic.client.TAMClient
import liltojustice.trueadaptivemusic.client.trigger.MusicTrigger

abstract class MusicEvent: MusicTrigger<MusicEvent.Parameters>() {
    init {
        parameters = Parameters.default()
    }

    open fun validate(vararg eventArgs: Any?): Boolean {
        return true
    }

    final override fun initParams(json: JsonObject) {
        parameters = json.get("parameters")
            ?.let { Gson().fromJson<Parameters>(it, Parameters::class.java) } ?: Parameters()
    }

    final override fun getTypeName(): String {
        return if (this is ErrorEvent)
            ErrorEvent.NAME
        else
            TAMClient.eventRegistry[this::class]
    }

    companion object: MusicEventCompanion<MusicEvent> {
    }

    data class Parameters(var isPersistent: Boolean = false): MusicTrigger.Parameters() {
        companion object: ParametersCompanion<Parameters> {
            override fun default(): Parameters {
                return Parameters()
            }
        }
    }

    interface MusicEventCompanion<TSelf>: MusicTriggerCompanion<MusicEvent> where TSelf: MusicEvent {
        fun invokeMusicEvent(eventName: String, vararg eventArgs: Any?) {
            InvokeMusicEventCallback.EVENT.invoker().invokeMusicEvent(eventName, *eventArgs)
        }
    }
}