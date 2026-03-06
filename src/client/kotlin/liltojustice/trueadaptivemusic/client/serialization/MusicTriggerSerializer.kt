package liltojustice.trueadaptivemusic.client.serialization

import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import liltojustice.trueadaptivemusic.client.Serialize
import liltojustice.trueadaptivemusic.client.TAMClient
import liltojustice.trueadaptivemusic.client.music.pack.MusicLoadException
import liltojustice.trueadaptivemusic.client.sound.SoundLibrary
import liltojustice.trueadaptivemusic.client.sound.playable.PlayableSound
import liltojustice.trueadaptivemusic.client.trigger.MusicTrigger
import liltojustice.trueadaptivemusic.client.trigger.MusicTriggerException
import liltojustice.trueadaptivemusic.client.trigger.event.ErrorEvent
import liltojustice.trueadaptivemusic.client.trigger.event.MusicEvent
import liltojustice.trueadaptivemusic.client.trigger.predicate.ErrorPredicate
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.util.JsonHelper
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.primaryConstructor

object MusicTriggerSerializer {
    class MusicPredicateTypeAdapter(private val soundLibrary: SoundLibrary?): TypeAdapter<MusicPredicate>() {
        override fun write(output: JsonWriter, predicate: MusicPredicate) {
            getGson().toJson(serialize(predicate), output)
        }

        override fun read(input: JsonReader): MusicPredicate? {
            return soundLibrary?.let { soundLibrary ->
                deserializePredicate(JsonParser.parseReader(input).asJsonObject, soundLibrary)
            }
        }
    }

    class MusicEventTypeAdapter(private val soundLibrary: SoundLibrary?): TypeAdapter<MusicEvent>() {
        override fun write(output: JsonWriter, event: MusicEvent) {
            getGson().toJson(serialize(event), output)
        }

        override fun read(input: JsonReader): MusicEvent? {
            return soundLibrary?.let { soundLibrary ->
                deserializeEvent(JsonParser.parseReader(input).asJsonObject, soundLibrary)
            }
        }
    }

    private fun serialize(predicate: MusicPredicate): JsonObject {
        (predicate as? ErrorPredicate)?.let { return it.actualJson }

        return getGson().toJsonTree(predicate).asJsonObject
    }

    private fun serialize(event: MusicEvent): JsonObject {
        (event as? ErrorEvent)?.let { return it.actualJson }

        return getGson().toJsonTree(event).asJsonObject
    }

    private fun deserializePredicate(json: JsonObject, soundLibrary: SoundLibrary): MusicPredicate {
        return try {
            val typeName = JsonHelper.getString(json, "type")
            val type = TAMClient.predicateRegistry[typeName]

            val stateless = getGson(soundLibrary).fromJson(json, type.java)
            val result = stateless::class.constructors.firstOrNull()
                ?.call(*stateless.getTriggerArgs().map { arg -> arg.value }.toTypedArray())
                ?: throw MusicLoadException(
                    "Failed to deserialize type '$type' with json $json due to constructor failure.")
            result
        }
        catch (e: MusicTriggerException) {
            ErrorPredicate(json, e.message ?: "Unknown")
        }
    }

    private fun deserializeEvent(json: JsonObject, soundLibrary: SoundLibrary): MusicEvent {
        return try {
            val typeName = JsonHelper.getString(json, "type")
            val type = TAMClient.eventRegistry[typeName]

            val stateless = getGson(soundLibrary).fromJson(json, type.java)
            val result = stateless::class.constructors.firstOrNull()
                ?.call(*stateless.getTriggerArgs().map { arg -> arg.value }.toTypedArray())
                ?: throw MusicLoadException(
                    "Failed to deserialize type '$type' with json $json due to constructor failure.")
            result.music = stateless.music
            result
        }
        catch (e: MusicTriggerException) {
            ErrorEvent(json, e.message ?: "Unknown")
        }
    }

    private fun getGson(soundLibrary: SoundLibrary? = null): Gson {
        return GsonBuilder()
            .addDeserializationExclusionStrategy(MusicTriggerExclusionStrategy)
            .addSerializationExclusionStrategy(MusicTriggerExclusionStrategy)
            .registerTypeHierarchyAdapter(
                PlayableSound::class.java,
                PlayableSoundSerializer.PlayableSoundTypeAdapter(soundLibrary)
            )
            .create()
    }

    private object MusicTriggerExclusionStrategy: ExclusionStrategy {
        @OptIn(ExperimentalStdlibApi::class)
        override fun shouldSkipField(f: FieldAttributes): Boolean {
            if (!f.declaringClass.kotlin.isSubclassOf(MusicTrigger::class)) {
                return false
            }

            val kotlinAnnotations = f.declaringClass.kotlin.declaredMemberProperties
                .firstOrNull() { it.name == f.name }
                ?.annotations
            return f.annotations?.any { it is Serialize } != true &&
                    kotlinAnnotations?.any { it is Serialize } != true &&
                    f.declaringClass?.kotlin?.primaryConstructor?.parameters?.map { it.name }
                        ?.let {
                            it.none { name -> name == f.name }
                        }
                    ?: true
        }

        override fun shouldSkipClass(clazz: Class<*>?): Boolean {
            return false
        }
    }
}