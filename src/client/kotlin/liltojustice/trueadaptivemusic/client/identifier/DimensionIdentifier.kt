package liltojustice.trueadaptivemusic.client.identifier

import net.minecraft.client.MinecraftClient
import net.minecraft.registry.RegistryKeys
import net.minecraft.util.Identifier
import net.minecraft.world.dimension.DimensionTypes

class DimensionIdentifier(id: Identifier): TypedIdentifier(id) {
    override fun toPrefixedTranslationKey(): String {
        return id.toTranslationKey("dimension")
    }

    companion object: TypedIdentifierCompanion() {
        override fun getRegistryIds(): List<Identifier> {
            return MinecraftClient
                .getInstance().world?.registryManager?.get(RegistryKeys.DIMENSION_TYPE)?.ids
                ?.filter { it != DimensionTypes.OVERWORLD_CAVES.value }
                ?.toList() ?: listOf()
        }
    }
}