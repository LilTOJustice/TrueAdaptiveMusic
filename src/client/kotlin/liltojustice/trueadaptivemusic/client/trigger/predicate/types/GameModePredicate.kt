package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusicapi.trigger.TriggerArguments
import liltojustice.trueadaptivemusicapi.trigger.predicate.type.StaticPredicateType
import net.minecraft.client.Minecraft
import net.minecraft.world.level.GameType

class GameModePredicate: StaticPredicateType<GameModePredicate.Arguments>("game_mode") {
    override val argDescriptions: Map<String, String>
        get() = super.argDescriptions + mapOf(
            Arguments::gameMode.name to "Which game mode to be in for the music to play.")

    data class Arguments(val gameMode: GameType): TriggerArguments()

    override fun validate(arguments: Arguments): Boolean {
        val minecraft = Minecraft.getInstance()
        val currentGameMode = minecraft.connection
            ?.getPlayerInfo(minecraft.player?.uuid ?: return false)?.gameMode

        return currentGameMode == arguments.gameMode
    }
}