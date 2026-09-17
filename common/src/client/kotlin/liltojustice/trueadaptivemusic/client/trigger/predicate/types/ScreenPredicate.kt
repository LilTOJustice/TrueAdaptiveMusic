package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusic.client.gui.screen.EditPackScreen
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
                    "the music. A screen identifier must be a fully qualified class name, i.e. " +
                    "\"${EditPackScreen::class.qualifiedName}\" for the pack edit screen."
        )

    override fun test(arguments: Arguments): Boolean {
        val screen = Minecraft.getInstance().screen ?: return false

        return arguments.screens.isEmpty() || arguments.screens.any { screen::class.qualifiedName == it.id }
    }

    data class Arguments(val screens: List<ScreenIdentifier>): TriggerArguments()

    data class ScreenIdentifier(val id: String)
}