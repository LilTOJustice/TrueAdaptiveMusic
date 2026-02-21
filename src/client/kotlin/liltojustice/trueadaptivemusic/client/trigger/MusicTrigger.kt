package liltojustice.trueadaptivemusic.client.trigger

import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import liltojustice.trueadaptivemusic.Logger
import liltojustice.trueadaptivemusic.ReflectionHelper
import liltojustice.trueadaptivemusic.client.Serialize
import liltojustice.trueadaptivemusic.client.sound.SoundLibrary
import liltojustice.trueadaptivemusic.client.sound.playable.PlayableSound
import liltojustice.trueadaptivemusic.client.trigger.predicate.TriggerArg
import liltojustice.trueadaptivemusic.client.trigger.predicate.TriggerParam
import net.minecraft.text.Text
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.primaryConstructor

abstract class MusicTrigger<TParameters: MusicTrigger.Parameters> {
    @Serialize
    private val type = getTypeName()

    @Serialize
    var music: List<PlayableSound> = emptyList()

    @Serialize
    lateinit var parameters: TParameters

    abstract fun getTypeName(): String

    fun getTriggerArgs(): List<TriggerArg> {
        return ReflectionHelper.getConstructorParameterValues(this)
            .map { arg -> TriggerArg(arg.name, arg.value) }
    }

    fun getTriggerId(): String {
        val args = getTriggerArgs()
        return getTypeName()  + if (args.isEmpty()) "" else "{${args.joinToString(",")}}"
    }

    open fun toJson(): JsonObject {
        return getGson().toJsonTree(this).asJsonObject
    }

    companion object {
        fun getGson(soundLibrary: SoundLibrary? = null): Gson {
            return GsonBuilder()
                .addDeserializationExclusionStrategy(MusicTriggerExclusionStrategy)
                .addSerializationExclusionStrategy(MusicTriggerExclusionStrategy)
                .registerTypeHierarchyAdapter(
                    PlayableSound::class.java, PlayableSoundTypeAdapter(soundLibrary))
                .create()
        }

        fun getTruncatedTriggerId(triggerId: String): String {
            val arrays = Regex("\\[[^]]*]").findAll(triggerId).map { result -> result.value }
            val text = arrays.fold(triggerId) { partial: String, array ->
                partial.replace(array, Regex(",.*").replace(array, ", ...]"))
            }

            return text
        }
    }

    interface MusicTriggerCompanion {
        val displayName: String?
            get() = null

        val argDisplayNames: Map<String, String>
            get() = mapOf()

        val argDescriptions: Map<String, String>
            get() = mapOf()

        fun getDisplayName(triggerName: String): Text
        fun getArgDisplayName(triggerName: String, argName: String): Text?
        fun getArgDescription(triggerName: String, argName: String): Text?
    }

    abstract class Parameters {
        fun getTriggerParams(): List<TriggerParam> {
            return ReflectionHelper.getConstructorParameterValues(this)
                .map { arg -> TriggerParam(arg.name, arg.value) }
        }

        fun initializeCopyFromArgs(vararg constructorArgs: Any): Parameters {
            return (this::class.primaryConstructor?.call(*constructorArgs) ?: default())
        }

        companion object: ParametersCompanion<Parameters> {
            override fun default(): Parameters {
                throw MusicTriggerException("default() called on abstract Parameters class.")
            }
        }

        interface ParametersCompanion<TSelf: Parameters> {
            val displayNames: Map<String, String>
                get() = mapOf()

            val descriptions: Map<String, String>
                get() = mapOf()

            fun default(): TSelf
        }
    }

    object MusicTriggerExclusionStrategy: ExclusionStrategy {
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

    class PlayableSoundTypeAdapter(private val soundLibrary: SoundLibrary?): TypeAdapter<PlayableSound>() {
        override fun write(output: JsonWriter, sound: PlayableSound) {
            output.value(sound.getSoundName())
        }

        override fun read(input: JsonReader): PlayableSound? {
            val path = input.nextString()
            val library = soundLibrary
                ?: throw MusicTriggerException("No sound library given for deserializing sound files from trigger.")
            return PlayableSound.of(path, library)
                ?: run {
                    Logger.logWarning("Could not find sound for \"$path\", skipping...")
                    null
                }
        }
    }
}
