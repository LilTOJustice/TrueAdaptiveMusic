package liltojustice.trueadaptivemusic.client.identifier

import net.minecraft.client.MinecraftClient
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.util.Identifier
import net.minecraft.world.biome.Biome
import kotlin.jvm.optionals.getOrNull

class BiomeIdentifier(id: Identifier): TypedIdentifier(id) {
    override fun toPrefixedLanguageKey(): String {
        return id.toTranslationKey("biome")
    }

    fun matches(biome: RegistryEntry<Biome>): Boolean {
        val registry = getBiomeRegistry() ?: return false
        return registry.tags.toList().firstOrNull { it.tag.id == id }?.tag?.let {
            biome.isIn(it)
        } ?: (registry[id] == biome.value())
    }

    companion object: TypedIdentifierCompanion() {
        override fun getRegistryIds(): List<Identifier> {
            val registry = getBiomeRegistry() ?: return emptyList()

            return registry.keys.toList().map { it.value } +
                    registry.tags.map { it.tag.id }.filter { it?.namespace != "c" }.toList()
        }

        private fun getBiomeRegistry(): Registry<Biome>? {
            return MinecraftClient.getInstance().world
                ?.registryManager
                ?.getOptional(RegistryKeys.BIOME)
                ?.getOrNull()
        }
    }
}