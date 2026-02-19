package liltojustice.trueadaptivemusic.client.identifier

import net.minecraft.client.MinecraftClient
import net.minecraft.registry.RegistryKeys
import net.minecraft.util.Identifier
import kotlin.jvm.optionals.getOrNull

class BiomeIdentifier(id: String): TypedIdentifier(id) {
    override fun toPrefixedTranslationKey(): String {
        return identifier.toTranslationKey("biome")
    }

    companion object: TypedIdentifierCompanion() {
        override fun getRegistryIds(): List<Identifier> {
            return MinecraftClient
                .getInstance().world?.registryManager
                ?.getOptional(RegistryKeys.BIOME)
                ?.getOrNull()
                ?.ids
                ?.toList()
                ?: listOf()
        }
    }
}