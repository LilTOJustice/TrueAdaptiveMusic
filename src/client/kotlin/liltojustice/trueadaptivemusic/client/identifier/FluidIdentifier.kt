package liltojustice.trueadaptivemusic.client.identifier

import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.Identifier

class FluidIdentifier(id: Identifier): TypedIdentifier(id) {
    override fun toPrefixedLanguageKey(): String {
        return id.toLanguageKey("fluid")
    }

    companion object: TypedIdentifierCompanion() {
        override fun getRegistryIds(): List<Identifier> {
            return BuiltInRegistries.FLUID.keySet().toList().filter { it != BuiltInRegistries.FLUID.defaultKey }
        }
    }
}