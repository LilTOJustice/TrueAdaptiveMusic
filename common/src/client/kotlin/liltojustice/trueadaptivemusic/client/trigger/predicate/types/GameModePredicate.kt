package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusicapi.trigger.arguments.TriggerArguments
import liltojustice.trueadaptivemusicapi.trigger.predicate.type.StaticPredicateType
import net.minecraft.client.Minecraft
import net.minecraft.world.level.GameType
import kotlin.reflect.typeOf

object GameModePredicate: StaticPredicateType<GameModePredicate.Arguments>(
    "game_mode", typeOf<Arguments>()
) {
    override val argDescriptions: Map<String, String>
        get() = super.argDescriptions + mapOf(
            Arguments::gameMode.name to "Which game mode to be in for the music to play.")

    data class Arguments(val gameMode: GameType): TriggerArguments()

    override fun test(arguments: Arguments): Boolean {
        val minecraft = Minecraft.getInstance()
        val currentGameMode = minecraft.connection
            ?.getPlayerInfo(minecraft.player?.uuid ?: return false)?.gameMode

        return currentGameMode == arguments.gameMode
    }
}