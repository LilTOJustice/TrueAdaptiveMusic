package liltojustice.trueadaptivemusic.client.identifier

import net.minecraft.client.MinecraftClient
import net.minecraft.registry.RegistryKeys
import net.minecraft.util.Identifier

class DimensionIdentifier(id: String): TypedIdentifier(id) {
    companion object: TypedIdentifierCompanion<DimensionIdentifier>() {
        override fun getRegistryIds(): List<Identifier> {
            return MinecraftClient
                .getInstance().world?.registryManager?.get(RegistryKeys.DIMENSION_TYPE)?.ids?.toList() ?: listOf()
        }
    }
}