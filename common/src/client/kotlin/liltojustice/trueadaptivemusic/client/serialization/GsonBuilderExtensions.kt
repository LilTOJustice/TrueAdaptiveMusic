package liltojustice.trueadaptivemusic.client.serialization

import com.google.gson.GsonBuilder
import liltojustice.trueadaptivemusic.client.serialization.adapter.IdentifierTypeAdapter
import net.minecraft.resources.Identifier

fun GsonBuilder.addIdentifierSupport(): GsonBuilder {
    return registerTypeAdapter(Identifier::class.java, IdentifierTypeAdapter)
}