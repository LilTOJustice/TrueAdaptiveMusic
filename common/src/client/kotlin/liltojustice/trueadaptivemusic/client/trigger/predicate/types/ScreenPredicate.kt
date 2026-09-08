package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusicapi.trigger.arguments.TriggerArguments
import liltojustice.trueadaptivemusicapi.trigger.predicate.type.StaticPredicateType
import net.minecraft.client.Minecraft
import kotlin.reflect.typeOf

object ScreenPredicate: StaticPredicateType<ScreenPredicate.Arguments>(
    "screen", typeOf<Arguments>()
) {
    override val argDescriptions: Map<String, String>
        get() = super.argDescriptions + mapOf(
            Arguments::screens.name to "Select all screens the music should play for. If none, any screen will trigger " +
                    "the music."
        )

    override fun test(arguments: Arguments): Boolean {
        val screen = Minecraft.getInstance().gui.screen() ?: return false

        return arguments.screens.isEmpty() || arguments.screens.any { screen::class.qualifiedName == it.id }
    }

    data class Arguments(val screens: List<ScreenIdentifier>): TriggerArguments()

    data class ScreenIdentifier(val id: String)
}