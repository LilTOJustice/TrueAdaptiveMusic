package liltojustice.trueadaptivemusic.client.identifier

import net.minecraft.client.Minecraft
import net.minecraft.resources.Identifier
import kotlin.jvm.optionals.getOrNull

class BiomeIdentifier(id: Identifier): TypedIdentifier(id) {
    override fun toPrefixedLanguageKey(): String {
        return id.toLanguageKey("biome")
    }

    companion object: TypedIdentifierCompanion() {
        override fun getRegistryIds(): List<Identifier> {
            return Minecraft
                .getInstance().world?.registryManager
                ?.getOptional(RegistryKeys.BIOME)
                ?.getOrNull()
                ?.ids
                ?.toList()
                ?: listOf()
        }
    }
}