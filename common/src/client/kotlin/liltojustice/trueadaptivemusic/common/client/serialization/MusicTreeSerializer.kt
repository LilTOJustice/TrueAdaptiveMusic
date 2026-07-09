package liltojustice.trueadaptivemusic.common.client.serialization

import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.common.client.music.tree.MusicTree
import liltojustice.trueadaptivemusic.common.client.serialization.legacy.LegacyMusicTreeJsonConverter
import liltojustice.trueadaptivemusic.common.client.sound.SoundLibrary
import liltojustice.trueadaptivemusic.common.client.sound.playable.PlayableSound
import liltojustice.trueadaptivemusic.common.client.trigger.event.MusicEvent
import liltojustice.trueadaptivemusic.common.client.trigger.predicate.MusicPredicate
import liltojustice.trueadaptivemusic.common.client.util.NInt
import java.lang.reflect.Type

object MusicTreeSerializer {
    fun serialize(musicTree: MusicTree): JsonObject {
        return getGson().toJsonTree(musicTree).asJsonObject
    }

    fun deserialize(json: JsonObject, soundLibrary: SoundLibrary): MusicTree {
        val serializationVersion = if (!json.has("version"))
            1
        else
            json.getAsJsonPrimitive("version").asInt
        val toDeserialize = LegacyMusicTreeJsonConverter.convert(json, serializationVersion)
        val tree = getGson(soundLibrary).fromJson(toDeserialize, MusicTree::class.java)
        tree.initializeParents()

        return tree
    }

    private fun getGson(soundLibrary: SoundLibrary? = null): Gson {
        return GsonBuilder()
            .registerTypeHierarchyAdapter(
                MusicPredicate::class.java,
                MusicTriggerSerializer.MusicPredicateTypeAdapter()
            )
            .registerTypeHierarchyAdapter(
                MusicEvent::class.java,
                MusicTriggerSerializer.MusicEventTypeAdapter(soundLibrary)
            )
            .registerTypeHierarchyAdapter(
                PlayableSound::class.java,
                PlayableSoundSerializer.PlayableSoundTypeAdapter(soundLibrary)
            )
            .registerTypeAdapter(
                MusicTree.Node.Parameters::class.java,
                MusicTreeNodeParametersDeserializer
            )
            .registerTypeAdapter(NInt::class.java, NInt.NIntTypeAdapter)
            .addSerializationExclusionStrategy(MusicTreeNodeDeserializationStrategy)
            .create()
    }

    private object MusicTreeNodeDeserializationStrategy: ExclusionStrategy {
        override fun shouldSkipField(f: FieldAttributes): Boolean {
            return f.declaringClass == MusicTree.Node::class.java && f.name == "parent"
        }

        override fun shouldSkipClass(clazz: Class<*>?): Boolean {
            return false
        }
    }

    @Suppress("USELESS_ELVIS")
    private object MusicTreeNodeParametersDeserializer: JsonDeserializer<MusicTree.Node.Parameters> {
        override fun deserialize(
            json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): MusicTree.Node.Parameters {
            val result = MusicTree.Node.Parameters.jsonDecode(json.asJsonObject)
            result.loopStartPoints = result.loopStartPoints ?: mapOf()
            result.musicWeights = result.musicWeights ?: mapOf()

            return result
        }
    }
}