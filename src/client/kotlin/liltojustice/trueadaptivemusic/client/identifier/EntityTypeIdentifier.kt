package liltojustice.trueadaptivemusic.client.identifier

import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.Entity
import kotlin.jvm.optionals.getOrNull

class EntityTypeIdentifier(id: Identifier): TypedIdentifier(id) {
    override fun toPrefixedLanguageKey(): String {
        return id.toLanguageKey("entity")
    }

    fun matches(entity: Entity): Boolean {
        return Registries.ENTITY_TYPE.tags.toList().firstOrNull { it.tag.id == id }?.tag?.let {
            entity.type.isIn(it)
        } ?: (Registries.ENTITY_TYPE[id] == entity.type)
    }

    companion object: TypedIdentifierCompanion() {
        override fun getRegistryIds(): List<Identifier> {
            return BuiltInRegistries.ENTITY_TYPE.keySet().toList() +
                    BuiltInRegistries.ENTITY_TYPE.tags.map { it.key().location }.toList()
        }
    }
}