package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusicapi.trigger.arguments.TriggerArguments
import liltojustice.trueadaptivemusicapi.trigger.predicate.type.StaticPredicateType
import net.minecraft.client.MinecraftClient
import net.minecraft.world.GameMode
import kotlin.reflect.typeOf

object GameModePredicate: StaticPredicateType<GameModePredicate.Arguments>(
    "game_mode", typeOf<Arguments>()
) {
    override val argDescriptions: Map<String, String>
        get() = super.argDescriptions + mapOf(
            Arguments::gameMode.name to "Which game mode to be in for the music to play.")

    data class Arguments(val gameMode: GameMode): TriggerArguments()

    override fun test(arguments: Arguments): Boolean {
        return Minecraft.getInstance().player?.gameMode() == arguments.gameMode
    }
}