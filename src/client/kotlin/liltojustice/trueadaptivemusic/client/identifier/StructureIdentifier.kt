package liltojustice.trueadaptivemusic.client.identifier

import net.minecraft.client.Minecraft
import net.minecraft.core.registries.Registries
import net.minecraft.resources.Identifier
import kotlin.jvm.optionals.getOrNull

class StructureIdentifier(id: Identifier): TypedIdentifier(id) {
    override fun toPrefixedLanguageKey(): String {
        return id.toLanguageKey("structure")
    }

    companion object: TypedIdentifierCompanion() {
        override fun getRegistryIds(): List<Identifier> {
            return Minecraft.getInstance().server?.worlds
                ?.flatMap { world ->
                    world.structureAccessor.registryManager
                        .getOptional(Registries.STRUCTURE).getOrNull()?.ids ?: listOf() }
                ?.toSet()
                ?.toList()
                ?: emptyList()
        }
    }
}