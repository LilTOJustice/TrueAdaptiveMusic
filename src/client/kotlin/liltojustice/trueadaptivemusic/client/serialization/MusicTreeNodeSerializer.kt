package liltojustice.trueadaptivemusic.client.serialization

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import liltojustice.trueadaptivemusic.client.music.tree.MusicTree
import java.lang.reflect.Type

object MusicTreeNodeSerializer {
    object MusicTreeNodeDeserializer: JsonDeserializer<MusicTree.Node> {
        override fun deserialize(
            json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): MusicTree.Node {
            val result = context.deserialize<MusicTree.Node>(json, MusicTree.Node::class.java)

            return result
        }
    }
}