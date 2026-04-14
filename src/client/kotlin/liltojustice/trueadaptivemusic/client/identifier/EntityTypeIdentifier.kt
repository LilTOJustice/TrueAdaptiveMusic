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
        return BuiltInRegistries.ENTITY_TYPE[id].getOrNull()?.let {
            entity.`is`(it)
        } ?: BuiltInRegistries.ENTITY_TYPE.tags.toList().firstOrNull { it.key().location == id }?.key()?.let {
            entity.`is`(it)
        } ?: false
    }

    companion object: TypedIdentifierCompanion() {
        override fun getRegistryIds(): List<Identifier> {
            return BuiltInRegistries.ENTITY_TYPE.keySet().toList() +
                    BuiltInRegistries.ENTITY_TYPE.tags.map { it.key().location }.toList()
        }
    }
}