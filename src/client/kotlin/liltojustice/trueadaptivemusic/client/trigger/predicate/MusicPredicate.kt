package liltojustice.trueadaptivemusic.client.trigger.predicate

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.TAMClient
import liltojustice.trueadaptivemusic.client.sound.playable.PlayableSound
import liltojustice.trueadaptivemusic.client.trigger.MusicTrigger
import net.minecraft.client.MinecraftClient

abstract class MusicPredicate: MusicTrigger<MusicPredicate.Parameters>() {
    init {
        parameters = Parameters.default()
    }

    private var lastResult = false
    private var ticksSinceResult = getFixedTickRate()
    var ambience = listOf<PlayableSound>()

    protected abstract fun test(client: MinecraftClient): Boolean

    final override fun getTypeName(): String {
        return if (this is ErrorPredicate)
            ErrorPredicate.NAME
        else
            TAMClient.predicateRegistry[this::class]
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

    final override fun toJsonFull(): JsonObject {
        val result = super.toJsonFull()
        val jsonMusicPath = JsonArray(music.size)
        ambience.forEach { sound -> jsonMusicPath.add(sound.getSoundName()) }
        result.add("ambiencePath", jsonMusicPath)

        return result
    }

    fun resetTicks() {
        lastResult = false
        ticksSinceResult = getFixedTickRate()
    }

    fun testPredicate(client: MinecraftClient): Boolean {
        val tickRate = getFixedTickRate()
        if (ticksSinceResult++ == tickRate) {
            ticksSinceResult = 1

            lastResult = test(client)
        }

        return lastResult
    }

    open fun getTickRate(): Int {
        return 20
    }

    private fun getFixedTickRate(): Int {
        val desiredTickRate = getTickRate()
        return if (desiredTickRate < 1) 0 else desiredTickRate
    }

    companion object: MusicPredicateCompanion<MusicPredicate> {
    }

    data class Parameters(
        var trackDelay: UInt = 0U,
        var trackDelayNoise: UInt = 0U,
        var enterDelay: UInt = 0U,
        var inheritMusic: Boolean = false,
        var inheritAmbience: Boolean = true)
        : MusicTrigger.Parameters() {
        companion object: ParametersCompanion<Parameters> {
            override fun default(): Parameters {
                return Parameters()
            }
        }
    }

    interface MusicPredicateCompanion<TSelf>: MusicTriggerCompanion<MusicPredicate>
            where TSelf: MusicPredicate {
    }
}