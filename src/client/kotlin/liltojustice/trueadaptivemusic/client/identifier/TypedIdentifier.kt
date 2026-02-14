package liltojustice.trueadaptivemusic.client.identifier

import net.minecraft.util.Identifier
import kotlin.reflect.KType
import kotlin.reflect.full.*

sealed class TypedIdentifier(id: String): Identifier(id) {
    override fun equals(other: Any?): Boolean {
        return super.equals(other) || (other as? TypedIdentifier)?.toString() == toString()
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

    override fun hashCode(): Int {
        var result = toString().hashCode()
        result = 31 * result + path.hashCode()
        result = 31 * result + namespace.hashCode()
        return result
    }
}