package liltojustice.trueadaptivemusic.client.trigger.event

import com.google.gson.Gson
import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.ReflectionHelper
import liltojustice.trueadaptivemusic.client.InvokeMusicEventCallback
import liltojustice.trueadaptivemusic.client.TAMClient
import liltojustice.trueadaptivemusic.text.translatableWithFallbackOrNull
import liltojustice.trueadaptivemusic.client.trigger.MusicTrigger
import liltojustice.trueadaptivemusic.client.trigger.TriggerReflectionHelper
import liltojustice.trueadaptivemusic.text.prettify
import net.minecraft.text.Text
import kotlin.collections.plus
import kotlin.reflect.full.declaredMembers

abstract class MusicEvent: MusicTrigger<MusicEvent.Parameters>() {
    init {
        parameters = Parameters.default()
    }

    open fun validate(vararg eventArgs: Any?): Boolean {
        return true
    }

    final override fun initParams(json: JsonObject) {
        val gson = Gson()
        val default = Parameters.default()
        val parametersJson = json.get("parameters").asJsonObject
        default.getTriggerParams().forEach {
            if (!parametersJson.has(it.name)) {
                val jsonRep = gson.toJsonTree(it.value)
                parametersJson.add(
                    it.name,
                    if (jsonRep.isJsonObject) jsonRep.asJsonObject.getAsJsonPrimitive("data") else jsonRep.asJsonPrimitive)
            }
        }
        parameters = gson.fromJson<Parameters>(parametersJson, Parameters::class.java)
    }

    final override fun getTypeName(): String {
        return if (this is ErrorEvent)
            ErrorEvent.NAME
        else
            TAMClient.eventRegistry[this::class]
    }

    final override fun toJsonFull(): JsonObject {
        return super.toJsonFull()
    }

    companion object: MusicEventCompanion<MusicEvent> {
    }

    data class Parameters(var isPersistent: Boolean = false): MusicTrigger.Parameters() {
        companion object: ParametersCompanion<Parameters> {
            override val displayNames: Map<String, String>
                get() = super.displayNames +
                        Parameters::class.declaredMembers.map { it.name }.associateWith { it.prettify() }

            override val descriptions: Map<String, String>
                get() = super.descriptions + mapOf(
                    "isPersistent" to "Don't stop this event's music after leaving this predicate.")

            override fun default(): Parameters {
                return Parameters()
            }
        }
    }

    interface MusicEventCompanion<TSelf>: MusicTriggerCompanion<MusicEvent> where TSelf: MusicEvent {
        override fun getDisplayName(triggerName: String): Text {
            return Text.translatableWithFallback(
                "trueadaptivemusic:event_${triggerName}_display",
                displayName ?: triggerName.prettify()
            )
        }

        override fun getArgDisplayName(triggerName: String, argName: String): Text? {
            val eventType = TAMClient.eventRegistry[triggerName]
            val inferredDisplayNames = ReflectionHelper.getConstructorParameterNames(eventType)
            val combined = inferredDisplayNames.associateWith { it.prettify() } +
                    TriggerReflectionHelper.getMusicTriggerArgDisplayNames(eventType)
            return translatableWithFallbackOrNull(
                "trueadaptivemusic:predicate_arg_${triggerName}_${argName}_display",
                combined[argName]
            )
        }

        override fun getArgDescription(triggerName: String, argName: String): Text? {
            return translatableWithFallbackOrNull(
                "trueadaptivemusic:event_arg_${triggerName}_${argName}_description",
                TriggerReflectionHelper.getMusicTriggerArgDescriptions(
                    TAMClient.eventRegistry[triggerName])[argName])
        }

        fun invokeMusicEvent(eventName: String, vararg eventArgs: Any?) {
            InvokeMusicEventCallback.EVENT.invoker().invokeMusicEvent(eventName, *eventArgs)
        }
    }
}