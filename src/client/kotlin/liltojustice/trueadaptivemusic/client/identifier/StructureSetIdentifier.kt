package liltojustice.trueadaptivemusic.client.identifier

import net.minecraft.client.MinecraftClient
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry

class StructureSetIdentifier(id: String): TypedIdentifier(id) {
    companion object: TypedIdentifierCompanion<StructureSetIdentifier>() {
        override fun getRegistryIds(): List<Identifier> {
            return MinecraftClient.getInstance().server?.worlds
                ?.flatMap { world -> world.registryManager.get(Registry.STRUCTURE_SET_KEY).ids }
                ?.toSet()
                ?.toList()
                ?: emptyList()
        }
    }
}