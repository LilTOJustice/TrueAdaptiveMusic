package liltojustice.trueadaptivemusic.client.identifier

import net.minecraft.entity.Entity
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier

class EntityTypeIdentifier(id: Identifier): TypedIdentifier(id) {
    override fun toPrefixedTranslationKey(): String {
        return id.toTranslationKey("entity")
    }

    fun matches(entity: Entity): Boolean {
        return Registries.ENTITY_TYPE.tags.toList().firstOrNull { it.tag.id == id }?.tag?.let {
            entity.type.isIn(it)
        } ?: (Registries.ENTITY_TYPE[id] == entity.type)
    }

    companion object: TypedIdentifierCompanion() {
        override fun getRegistryIds(): List<Identifier> {
            return Registries.ENTITY_TYPE.keys.map { it.value }.toList() +
                    Registries.ENTITY_TYPE.tags.map { it.tag.id }.toList()
        }
    }
}