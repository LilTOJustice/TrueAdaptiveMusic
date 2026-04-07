package liltojustice.trueadaptivemusic.client.identifier

import net.minecraft.client.Minecraft
import net.minecraft.core.registries.Registries
import net.minecraft.resources.Identifier
import net.minecraft.world.level.dimension.BuiltinDimensionTypes
import kotlin.jvm.optionals.getOrNull

class DimensionIdentifier(id: Identifier): TypedIdentifier(id) {
    override fun toPrefixedLanguageKey(): String {
        return id.toLanguageKey("dimension")
    }

    companion object: TypedIdentifierCompanion() {
        override fun getRegistryIds(): List<Identifier> {
            return Minecraft
                .getInstance()
                .level
                ?.registryAccess()
                ?.lookup(Registries.DIMENSION_TYPE)
                ?.getOrNull()
                ?.keySet()
                ?.filter { it != BuiltinDimensionTypes.OVERWORLD_CAVES.identifier() }
                ?.toList()
                ?: emptyList()
        }
    }
}