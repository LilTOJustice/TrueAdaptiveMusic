package liltojustice.trueadaptivemusic.client.serialization

import com.google.gson.GsonBuilder
import liltojustice.trueadaptivemusic.client.serialization.adapter.IdentifierTypeAdapter
import net.minecraft.resources.ResourceLocation

fun GsonBuilder.addIdentifierSupport(): GsonBuilder {
    return registerTypeAdapter(ResourceLocation::class.java, IdentifierTypeAdapter)
}