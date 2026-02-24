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
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import liltojustice.trueadaptivemusic.client.music.tree.MusicTree
import liltojustice.trueadaptivemusic.client.sound.SoundLibrary
import liltojustice.trueadaptivemusic.client.trigger.MusicTrigger
import liltojustice.trueadaptivemusic.client.sound.playable.PlayableSound
import liltojustice.trueadaptivemusic.client.trigger.event.MusicEvent
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import kotlin.reflect.full.isSubclassOf

object MusicTreeSerializer {
    fun serialize(musicTree: MusicTree): JsonObject {
        return getGson().toJsonTree(musicTree).asJsonObject
    }

    fun deserialize(json: JsonObject, soundLibrary: SoundLibrary): MusicTree {
        val tree = getGson(soundLibrary).fromJson(json, MusicTree::class.java)
        tree.initializeParents()

        return tree
    }

    private fun getGson(soundLibrary: SoundLibrary? = null): Gson {
        return GsonBuilder()
            .registerTypeHierarchyAdapter(
                MusicPredicate::class.java,
                MusicTriggerSerializer.MusicPredicateTypeAdapter(soundLibrary)
            )
            .registerTypeHierarchyAdapter(
                MusicEvent::class.java, MusicTriggerSerializer.MusicEventTypeAdapter(soundLibrary))
            .registerTypeHierarchyAdapter(
                PlayableSound::class.java,
                PlayableSoundSerializer.PlayableSoundTypeAdapter(soundLibrary)
            )
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
}