package liltojustice.trueadaptivemusic.client.util

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter

data class TimeOfDay(val value: UInt) {
    companion object {
        const val MAX_VALUE = 23999U
    }

    object TimeOfDayTypeAdapter: TypeAdapter<TimeOfDay>() {
        override fun write(p0: JsonWriter, p1: TimeOfDay?) {
            p0.value(p1?.value?.toInt() ?: 0)
        }

        override fun read(p0: JsonReader): TimeOfDay {
            return TimeOfDay(p0.nextInt().toUInt())
        }
    }
}