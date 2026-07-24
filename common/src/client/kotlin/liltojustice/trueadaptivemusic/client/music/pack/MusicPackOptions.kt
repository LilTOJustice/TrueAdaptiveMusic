package liltojustice.trueadaptivemusic.client.music.pack

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import liltojustice.trueadaptivemusic.ReflectionHelper
import liltojustice.trueadaptivemusic.text.StringExtensions.prettify
import liltojustice.trueadaptivemusic.text.translatableWithFallbackOrNull
import net.minecraft.network.chat.Component
import liltojustice.trueadaptivemusicapi.identifier.MusicSoundEventIdentifier
import kotlin.reflect.KParameter
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.javaType
import kotlin.reflect.typeOf

data class MusicPackOptions(
    val description: String = "",
    val persistentNodeMusic: Boolean = false,
    val prioritySoundEvents: List<MusicSoundEventIdentifier> = emptyList()
) {
    fun getArgs(): List<Any?> {
        return ReflectionHelper.getConstructorParameterValues(this).map { param -> param.value }
    }

    fun jsonEncode(): String {
        return json.toJson(this)
    }

    companion object {
        private val displayNames = MusicPackOptions::class
            .primaryConstructor
            ?.parameters
            ?.mapNotNull { it.name }
            ?.associateWith { it.prettify() } ?: mapOf()

        private val descriptions = mapOf(
            MusicPackOptions::description.name to "Description of the Music Pack.",
            MusicPackOptions::persistentNodeMusic.name to "If checked, music from the current node will continue to " +
                    "play until it finishes if another node is chosen. Disables music fading between nodes."
        )

        private val json = GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapterFactory(ListAdapterFactory)
            .create()

        fun jsonDecode(string: String): MusicPackOptions {
            return json.fromJson(string, MusicPackOptions::class.java)
        }

        fun getRequiredArgs(): List<KParameter> {
            return MusicPackOptions::class.primaryConstructor?.parameters ?: emptyList()
        }

        fun getArgDisplayName(argName: String): Component? {
            return translatableWithFallbackOrNull(
                "trueadaptivemusic.pack_options.${argName}.display", displayNames[argName])
        }

        fun getArgDescription(argName: String): Component? {
            return translatableWithFallbackOrNull(
                "trueadaptivemusic.pack_options.${argName}.description", descriptions[argName])
        }
    }

    private class SoundEventIdentifierListInstanceCreator(
        private val gson: Gson
    ): TypeAdapter<List<MusicSoundEventIdentifier>>() {
        override fun write(p0: JsonWriter, p1: List<MusicSoundEventIdentifier>) {
            p0.jsonValue(gson.toJson(p1))
        }

        @OptIn(ExperimentalStdlibApi::class)
        override fun read(p0: JsonReader): List<MusicSoundEventIdentifier> {
            return gson.fromJson(p0, typeOf<List<MusicSoundEventIdentifier>>().javaType)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private object ListAdapterFactory: TypeAdapterFactory {
        override fun <T> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? {
            val rawType = type.rawType
            if (rawType.isAssignableFrom(List::class.java) &&
                rawType.kotlin.typeParameters.firstOrNull()?.starProjectedType == typeOf<MusicSoundEventIdentifier>()) {
                return SoundEventIdentifierListInstanceCreator(gson) as? TypeAdapter<T>
            }

            return null
        }
    }
}
