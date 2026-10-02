package liltojustice.trueadaptivemusic.client.serialization.adapter

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import net.minecraft.resources.ResourceLocation

object IdentifierTypeAdapter: TypeAdapter<ResourceLocation>() {
    override fun write(writer: JsonWriter, id: ResourceLocation) {
        writer.beginObject()
        writer.name("namespace").value(id.namespace)
        writer.name("path").value(id.path)
        writer.endObject()
    }

    override fun read(reader: JsonReader): ResourceLocation {
        reader.beginObject()

        if (!reader.hasNext()) {
            reader.endObject()

            return ResourceLocation.fromNamespaceAndPath("null", "null")
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

        return ResourceLocation.fromNamespaceAndPath(namespace, path)
    }
}