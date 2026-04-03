package liltojustice.trueadaptivemusic.client.identifier

import net.minecraft.client.Minecraft
import net.minecraft.core.registries.Registries
import net.minecraft.resources.Identifier
import kotlin.jvm.optionals.getOrNull

class StructureSetIdentifier(id: Identifier): TypedIdentifier(id) {
    override fun toPrefixedLanguageKey(): String {
        return id.toLanguageKey("structure_set")
    }

    companion object: TypedIdentifierCompanion() {
        override fun getRegistryIds(): List<Identifier> {
            return Minecraft.getInstance().server?.worlds
                ?.flatMap { world ->
                    world.structureAccessor.registryManager
                        .getOptional(Registries.STRUCTURE_SET).getOrNull()?.ids ?: listOf() }
                ?.toSet()
                ?.toList()
                ?: emptyList()
        }
    }
}