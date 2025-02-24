package liltojustice.trueadaptivemusic.client.identifier

import net.minecraft.client.MinecraftClient
import net.minecraft.registry.RegistryKeys
import net.minecraft.util.Identifier

class StructureIdentifier(id: String): TypedIdentifier(id) {
    companion object: TypedIdentifierCompanion<StructureIdentifier>() {
        override fun getRegistryIds(): List<Identifier> {
            return MinecraftClient.getInstance().server?.worlds
                ?.flatMap { world -> world.structureAccessor.registryManager.get(RegistryKeys.STRUCTURE).ids }
                ?.toSet()
                ?.toList()
                ?: emptyList()
        }
    }
}