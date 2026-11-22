package liltojustice.trueadaptivemusic.client.music.tree

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import liltojustice.trueadaptivemusic.client.util.PDouble
import liltojustice.trueadaptivemusic.client.util.WeightedList

typealias MusicWeightedList = WeightedList<String>

object MusicWeightedListTypeAdapter: TypeAdapter<MusicWeightedList>() {
    override fun write(out: JsonWriter, value: MusicWeightedList) {
        out.beginObject()

        value.weights.forEach {
            out.name(it.key)
            out.value(it.value)
        }

        out.endObject()
    }

    override fun read(reader: JsonReader): MusicWeightedList {
        val map = mutableMapOf<String, PDouble>()

        reader.beginObject()

        while (reader.peek() == JsonToken.NAME) {
            map[reader.nextName()] = PDouble(reader.nextDouble())
        }

        reader.endObject()

        return MusicWeightedList(map)
    }
}
