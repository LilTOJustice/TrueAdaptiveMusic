package liltojustice.trueadaptivemusic.client.identifier

import net.minecraft.client.MinecraftClient
import net.minecraft.registry.RegistryKeys
import net.minecraft.util.Identifier

class StructureSetIdentifier(id: Identifier): TypedIdentifier(id) {
    override fun toPrefixedTranslationKey(): String {
        return id.toTranslationKey("structure_set")
    }

    companion object: TypedIdentifierCompanion() {
        override fun getRegistryIds(): List<Identifier> {
            return MinecraftClient.getInstance().server?.worlds
                ?.flatMap { world -> world.structureAccessor.registryManager.get(RegistryKeys.STRUCTURE_SET).ids }
                ?.toSet()
                ?.toList()
                ?: emptyList()
        }
    }
}