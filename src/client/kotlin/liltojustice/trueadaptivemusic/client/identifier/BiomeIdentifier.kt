package liltojustice.trueadaptivemusic.client.identifier

import net.minecraft.client.MinecraftClient
import net.minecraft.registry.RegistryKeys
import net.minecraft.util.Identifier

class BiomeIdentifier(id: String): TypedIdentifier(id) {
    companion object: TypedIdentifierCompanion<BiomeIdentifier>() {
        override fun getRegistryIds(): List<Identifier> {
            return MinecraftClient
                .getInstance().world?.registryManager?.get(RegistryKeys.BIOME)?.ids?.toList() ?: listOf()
        }
    }
}