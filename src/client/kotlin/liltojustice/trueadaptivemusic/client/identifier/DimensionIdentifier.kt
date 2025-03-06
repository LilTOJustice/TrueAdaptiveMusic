package liltojustice.trueadaptivemusic.client.identifier

import net.minecraft.client.MinecraftClient
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry

class DimensionIdentifier(id: String): TypedIdentifier(id) {
    companion object: TypedIdentifierCompanion<DimensionIdentifier>() {
        override fun getRegistryIds(): List<Identifier> {
            return MinecraftClient
                .getInstance().world?.registryManager?.get(Registry.DIMENSION_TYPE_KEY)?.ids?.toList() ?: listOf()
        }
    }
}