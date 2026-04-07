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
            return Minecraft.getInstance().singleplayerServer?.allLevels
                ?.flatMap { level ->
                    level
                        .structureManager()
                        .registryAccess()
                        .lookup(Registries.STRUCTURE)
                        .getOrNull()
                        ?.keySet()
                        ?: emptyList()
                }
                ?.toSet()
                ?.toList()
                ?: emptyList()
        }
    }
}