package liltojustice.trueadaptivemusic.client.cache

import net.minecraft.world.entity.player.Player

class PlayerCombatTracker {
    private val playerCombatants = mutableMapOf<String, PlayerAttackState>()

    operator fun get(playerName: String): PlayerAttackState? {
        return playerCombatants[playerName]
    }

    fun hitBy(player: Player, tick: Int) {
        val playerName = player.name.string
        val clone = playerCombatants[playerName]?.copy(attacker = true, lastTick = tick) ?: PlayerAttackState(
            attackee = false,
            attacker = true,
            lastTick = tick
        )

        playerCombatants[playerName] = clone
    }

    fun hit(player: Player, tick: Int) {
        val playerName = player.name.string
        val clone = playerCombatants[playerName]?.copy(attackee = true, lastTick = tick) ?: PlayerAttackState(
            attackee = true,
            attacker = false,
            lastTick = tick
        )

        playerCombatants[playerName] = clone
    }

    fun remove(otherPlayerName: String) {
        playerCombatants.remove(otherPlayerName)
    }

    data class PlayerAttackState(val attackee: Boolean, val attacker: Boolean, val lastTick: Int) {
        fun getTicksSince(currentTick: Int): Int {
            return currentTick - lastTick
        }
    }
}