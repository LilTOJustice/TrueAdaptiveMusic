package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusic.Constants
import liltojustice.trueadaptivemusic.client.TAMClient
import liltojustice.trueadaptivemusic.client.network.TAMClientNetworking
import liltojustice.trueadaptivemusicapi.trigger.arguments.TriggerArguments
import liltojustice.trueadaptivemusicapi.trigger.predicate.type.StaticPredicateType
import net.minecraft.client.Minecraft
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.invariantSeparatorsPathString
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.readText
import kotlin.reflect.typeOf

object CustomPredicate: StaticPredicateType<CustomPredicate.Arguments>(
    "custom", typeOf<Arguments>()
) {
    override val argDescriptions: Map<String, String>
        get() = super.argDescriptions + mapOf(
            Arguments::predicateFile.name to "JSON file with the predicate logic. Should be located in " +
                    "'${Constants.PREDICATES_DIRNAME}' directory within this music pack."
        )
    override val tickRate: Int
        get() = super.tickRate * 10

    data class Arguments(val predicateFile: PredicateFile): TriggerArguments()

    override fun test(arguments: Arguments): Boolean {
        if (Minecraft.getInstance().player == null) {
            return false
        }

        val packPath = TAMClient.musicPack?.packPath?.invariantSeparatorsPathString ?: return false
        return Path(packPath, Constants.PREDICATES_DIRNAME, arguments.predicateFile.fileName)
            .takeIf { it.exists() }
            ?.let {
                TAMClientNetworking.queryCustomPredicate(
                    it.nameWithoutExtension, it.readText())
            } ?: false
    }

    data class PredicateFile(val fileName: String)
}