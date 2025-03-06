package liltojustice.trueadaptivemusic.client.identifier

import net.minecraft.client.MinecraftClient
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry

class BiomeIdentifier(id: String): TypedIdentifier(id) {
    companion object: TypedIdentifierCompanion<BiomeIdentifier>() {
        override fun getRegistryIds(): List<Identifier> {
            return MinecraftClient
                .getInstance().world?.registryManager?.get(Registry.BIOME_KEY)?.ids?.toList() ?: listOf()
        }
    }
}