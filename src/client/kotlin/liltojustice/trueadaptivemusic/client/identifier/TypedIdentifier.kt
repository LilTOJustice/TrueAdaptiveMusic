package liltojustice.trueadaptivemusic.client.identifier

import net.minecraft.util.Identifier
import kotlin.reflect.KType
import kotlin.reflect.full.*

sealed class TypedIdentifier(id: String) {
    val identifier: Identifier = Identifier.of(id)
    val path: String = identifier.path
    val namespace: String = identifier.namespace

    fun toTranslationKey(prefix: String): String {
        return identifier.toTranslationKey(prefix)
    }

    companion object: TypedIdentifierCompanion<TypedIdentifier>() {
        override fun getRegistryIds(): List<Identifier> {
            throw TypedIdentifierException(
                "Attempt to get type name from abstract ${TypedIdentifier::class.simpleName}.")
        }

        fun getRegistryIdsFromType(type: KType): List<Identifier> {
            val typeCompanion = TypedIdentifierCompanion::class.sealedSubclasses
                .firstOrNull { subclass -> subclass.qualifiedName?.contains(type.toString()) ?: false }
                ?: throw TypedIdentifierException("Failed to find valid companion for $type. " +
                        "Ensure it has a companion object implementing the " +
                        "${TypedIdentifierCompanion::class.simpleName} interface.")
            return (typeCompanion.functions.firstOrNull { f -> f.name == Companion::getRegistryIds.name }
                ?.call(typeCompanion.objectInstance) as? List<*>)?.mapNotNull { x -> x as? Identifier }
                ?: throw TypedIdentifierException(
                    "Failed to get registry ids from identifier type ${type}. " +
                            "Ensure it has a companion object implementing the " +
                            "${TypedIdentifierCompanion::class.simpleName} interface.")
        }
    }

    sealed class TypedIdentifierCompanion<TSelf> where TSelf: TypedIdentifier {
        abstract fun getRegistryIds(): List<Identifier>
        fun initializeFromIdString(type: KType, id: String): TypedIdentifier {
            return TypedIdentifier::class.sealedSubclasses
                .firstOrNull { subclass ->
                    subclass.createType(type.arguments, type.isMarkedNullable, type.annotations) == type }
                ?.primaryConstructor?.call(id)
                ?: throw TypedIdentifierException("Failed to initialize ${this::class.simpleName} from id $id")
        }
    }

    override fun toString(): String {
        return identifier.toString()
    }
}