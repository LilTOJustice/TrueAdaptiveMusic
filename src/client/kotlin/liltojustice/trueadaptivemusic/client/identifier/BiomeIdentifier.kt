package liltojustice.trueadaptivemusic.client.identifier

import net.minecraft.client.Minecraft
import net.minecraft.core.Holder
import net.minecraft.core.Registry
import net.minecraft.core.registries.Registries
import net.minecraft.resources.Identifier
import net.minecraft.world.level.biome.Biome
import kotlin.jvm.optionals.getOrNull

class BiomeIdentifier(id: Identifier): TypedIdentifier(id) {
    override fun toPrefixedLanguageKey(): String {
        return id.toLanguageKey("biome")
    }

    fun matches(biome: Holder<Biome>): Boolean {
        val registry = getBiomeRegistry() ?: return false

        return registry[id].getOrNull()?.let {
            biome.`is`(it.key())
        } ?: registry.tags.toList().firstOrNull { it.key().location == id }?.key()?.let {
            biome.`is`(it)
        } ?: false
    }

    companion object: TypedIdentifierCompanion() {
        override fun getRegistryIds(): List<Identifier> {
            val registry = getBiomeRegistry() ?: return emptyList()

            return registry.keySet().toList() +
                    registry.tags.map { it.key().location }.filter { it.namespace != "c" }.toList()
        }

        private fun getBiomeRegistry(): Registry<Biome>? {
            return Minecraft.getInstance().level
                ?.registryAccess()
                ?.lookup(Registries.BIOME)
                ?.getOrNull()
        }
    }
}