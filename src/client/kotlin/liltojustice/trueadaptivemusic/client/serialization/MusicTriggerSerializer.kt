package liltojustice.trueadaptivemusic.client.serialization

import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import liltojustice.trueadaptivemusic.client.Serialize
import liltojustice.trueadaptivemusic.client.TAMClient
import liltojustice.trueadaptivemusic.client.sound.SoundLibrary
import liltojustice.trueadaptivemusic.client.sound.playable.PlayableSound
import liltojustice.trueadaptivemusic.client.trigger.MusicTrigger
import liltojustice.trueadaptivemusic.client.trigger.MusicTriggerException
import liltojustice.trueadaptivemusic.client.trigger.event.ErrorEvent
import liltojustice.trueadaptivemusic.client.trigger.event.MusicEvent
import liltojustice.trueadaptivemusic.client.trigger.predicate.ErrorPredicate
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import liltojustice.trueadaptivemusic.client.util.NInt
import liltojustice.trueadaptivemusicapi.TAMAPI
import liltojustice.trueadaptivemusicapi.trigger.arguments.TriggerArguments
import net.minecraft.resources.Identifier
import net.minecraft.util.GsonHelper
import java.lang.reflect.Type
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.primaryConstructor

object MusicTriggerSerializer {
    class MusicPredicateTypeAdapter: TypeAdapter<MusicPredicate<*>>() {
        override fun write(output: JsonWriter, predicate: MusicPredicate<*>) {
            getGson().toJson(serialize(predicate), output)
        }

        override fun read(input: JsonReader): MusicPredicate<*> {
            return deserializePredicate(JsonParser.parseReader(input).asJsonObject)
        }
    }

    class MusicEventTypeAdapter(private val soundLibrary: SoundLibrary?): TypeAdapter<MusicEvent<*>>() {
        override fun write(output: JsonWriter, event: MusicEvent<*>) {
            getGson().toJson(serialize(event), output)
        }

        override fun read(input: JsonReader): MusicEvent<*>? {
            return soundLibrary?.let { soundLibrary ->
                deserializeEvent(JsonParser.parseReader(input).asJsonObject, soundLibrary)
            }
        }
    }

    private fun serialize(predicate: MusicPredicate<*>): JsonObject {
        return (predicate.arguments as? ErrorPredicate.Arguments)?.let { return it.actualJson }
            ?: run {
                val gson = getGson()
                val result = JsonObject()
                result.addProperty("type", predicate.type.typeName)
                result.add("arguments", gson.toJsonTree(predicate.arguments))

                result
            }
    }

    private fun serialize(event: MusicEvent<*>): JsonObject {
        return (event.arguments as? ErrorEvent.Arguments)?.let { return it.actualJson }
            ?: run {
                val gson = getGson()
                val result = JsonObject()
                result.addProperty("type", event.type.typeName)
                result.add("arguments", gson.toJsonTree(event.arguments))

                val music = JsonArray()
                event.music.forEach { music.add(it.getSoundName()) }
                result.add("music", music)
                result.add("parameters", gson.toJsonTree(event.parameters))

                result
            }
    }

    private fun deserializePredicate(json: JsonObject): MusicPredicate<*> {
        return try {
            val typeName = GsonHelper.getAsString(json, "type")
            val type = TAMAPI.getPredicateType(typeName)
                ?: throw MusicTriggerException("Unknown predicate type '$typeName'")
            val arguments = type.createArguments(
                getGson(), json.getAsJsonObject("arguments") ?: JsonObject())

            TAMClient.musicPredicateFactory.fromArgs(type, arguments)
        }
        catch (e: MusicTriggerException) {
            TAMClient.musicPredicateFactory.fromArgs(
                ErrorPredicate,
                ErrorPredicate.Arguments(json, e.message ?: "Unknown")
            )
        }
    }

    private fun deserializeEvent(json: JsonObject, soundLibrary: SoundLibrary): MusicEvent<*> {
        val music = json.getAsJsonArray("music").mapNotNull {
            PlayableSound.of(it.asString, soundLibrary)
        }

        val parameters = getGson(null)
            .fromJson(json.getAsJsonObject("parameters") ?: JsonObject(),
                MusicEvent.Parameters::class.java)

        return try {
            val typeName = GsonHelper.getAsString(json, "type")
            val type = TAMAPI.getEventType(typeName)
                ?: throw MusicTriggerException("Unknown event type '$typeName'")
            val arguments = type.createArguments(
                getGson(soundLibrary), json.getAsJsonObject("arguments") ?: JsonObject())

            TAMClient.musicEventFactory.fromArgs(type, arguments, music, parameters)
        }
        catch (e: MusicTriggerException) {
            TAMClient.musicEventFactory.fromArgs(
                ErrorEvent,
                ErrorEvent.Arguments(json, e.message ?: "Unknown"),
                music,
                parameters
            )
        }
    }

    private fun getTriggerArgGsonBuilder(): GsonBuilder {
        return GsonBuilder()
            .registerTypeAdapter(NInt::class.java, NInt.NIntTypeAdapter)
            .registerTypeAdapter(Identifier::class.java, IdentifierTypeAdapter)
    }

    private fun getGson(soundLibrary: SoundLibrary? = null): Gson {
        return getTriggerArgGsonBuilder()
            .addDeserializationExclusionStrategy(MusicTriggerExclusionStrategy)
            .addSerializationExclusionStrategy(MusicTriggerExclusionStrategy)
            .registerTypeHierarchyAdapter(
                PlayableSound::class.java,
                PlayableSoundSerializer.PlayableSoundTypeAdapter(soundLibrary)
            )
            .registerTypeHierarchyAdapter(
                TriggerArguments::class.java, TriggerArgumentsDeserializer)
            .create()
    }

    private object MusicTriggerExclusionStrategy: ExclusionStrategy {
        @OptIn(ExperimentalStdlibApi::class)
        override fun shouldSkipField(f: FieldAttributes): Boolean {
            if (!f.declaringClass.kotlin.isSubclassOf(MusicTrigger::class)) {
                return false
            }

            val kotlinAnnotations = f.declaringClass.kotlin.declaredMemberProperties.firstOrNull { it.name == f.name }
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

    private object IdentifierTypeAdapter: TypeAdapter<Identifier>() {
        override fun write(writer: JsonWriter, id: Identifier) {
            writer.beginObject()
            writer.name("namespace").value(id.namespace)
            writer.name("path").value(id.path)
            writer.endObject()
        }

        override fun read(reader: JsonReader): Identifier {
            reader.beginObject()

            if (!reader.hasNext()) {
                reader.endObject()

                return Identifier.fromNamespaceAndPath("null", "null")
            }

            reader.nextName()
            val namespace = reader.nextString()
            reader.nextName()
            val path = reader.nextString()
            val next = reader.peek()
            if (next == JsonToken.NAME) {
                reader.nextName()
                reader.nextString()
            }

            reader.endObject()

            return Identifier.fromNamespaceAndPath(namespace, path)
        }
    }

    private object TriggerArgumentsDeserializer: JsonDeserializer<TriggerArguments> {
        override fun deserialize(p0: JsonElement, p1: Type, p2: JsonDeserializationContext): TriggerArguments {
            val gson = getTriggerArgGsonBuilder().create()
            val defaults = try {
                (p1 as Class<*>).kotlin.primaryConstructor?.callBy(mapOf())
            }
            catch (_: Exception) {
                null
            }
                ?.let { gson.toJsonTree(it).asJsonObject.asMap() }
                ?: mapOf()

            val newValues = p0.asJsonObject.asMap()
            val allKeys = newValues.keys + defaults.keys
            val mergedMap = mutableMapOf<String, JsonElement>()
            allKeys.forEach { key ->
                mergedMap[key] = newValues[key]?.takeIf { !it.isJsonNull } ?: defaults[key] ?: JsonNull.INSTANCE
            }

            val result = JsonObject()
            mergedMap.forEach { (key, element) ->
                result.add(key, element)
            }

            return gson.fromJson(result, p1)
        }
    }
}