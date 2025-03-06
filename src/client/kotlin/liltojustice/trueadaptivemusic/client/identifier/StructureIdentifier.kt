package liltojustice.trueadaptivemusic.client.identifier

import net.minecraft.client.MinecraftClient
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry

class StructureIdentifier(id: String): TypedIdentifier(id) {
    companion object: TypedIdentifierCompanion<StructureIdentifier>() {
        override fun getRegistryIds(): List<Identifier> {
            return MinecraftClient.getInstance().server?.worlds
                ?.flatMap { world -> world.registryManager.get(Registry.STRUCTURE_FEATURE_KEY).ids }
                ?.toSet()
                ?.toList()
                ?: emptyList()
        }
    }
}