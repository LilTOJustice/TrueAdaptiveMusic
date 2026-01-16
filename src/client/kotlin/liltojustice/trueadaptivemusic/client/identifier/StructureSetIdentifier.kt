package liltojustice.trueadaptivemusic.client.identifier

import net.minecraft.client.MinecraftClient
import net.minecraft.registry.RegistryKeys
import net.minecraft.util.Identifier
import kotlin.jvm.optionals.getOrNull

class StructureSetIdentifier(id: String): TypedIdentifier(id) {
    companion object: TypedIdentifierCompanion<StructureSetIdentifier>() {
        override fun getRegistryIds(): List<Identifier> {
            return MinecraftClient.getInstance().server?.worlds
                ?.flatMap { world ->
                    world.structureAccessor.registryManager
                        .getOptional(RegistryKeys.STRUCTURE_SET).getOrNull()?.ids ?: listOf() }
                ?.toSet()
                ?.toList()
                ?: emptyList()
        }
    }
}