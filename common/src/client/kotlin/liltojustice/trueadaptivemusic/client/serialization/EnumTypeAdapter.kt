package liltojustice.trueadaptivemusic.client.serialization

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import kotlin.reflect.KClass

class EnumTypeAdapter<T: Enum<*>>(private val enumClass: KClass<T>): TypeAdapter<Enum<*>>() {
    override fun write(writer: JsonWriter, value: Enum<*>) {
        writer.value(value.ordinal)
    }

    override fun read(reader: JsonReader): Enum<*>? {
        val ordinal = reader.nextInt()

        return enumClass.java.enumConstants[ordinal]
    }
}