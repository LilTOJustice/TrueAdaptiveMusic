package liltojustice.trueadaptivemusic.client.predicate

import com.google.gson.JsonObject
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.Entity
import net.minecraft.entity.mob.MobEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.Vec3i
import java.util.*
import kotlin.concurrent.schedule
import kotlin.math.abs
import kotlin.math.cbrt

class CombatPredicate internal constructor(partialPath: String)
    : MusicPredicate(partialPath) {
    private val aggroTimer: Timer = Timer()
    private var aggroTimerTask: TimerTask? = null
    private var isAggro: Boolean = false

    override fun test(client: MinecraftClient): Boolean {
        val playerEntity = client.player ?: return false
        val playerBlockPos = playerEntity.blockPos ?: return false
        val world = client.world ?: return false

        for (entity: Entity? in world.entities)
        {
            val mobEntity: MobEntity = entity as? MobEntity ?: continue
            if (mobEntity.attacking?.id == playerEntity.id || (mobEntity.isAttacking
                        && closeEnough(playerBlockPos, mobEntity.blockPos,
                    Vec3d(mobEntity.boundingBox.xLength,
                        mobEntity.boundingBox.yLength,
                        mobEntity.boundingBox.zLength))))
            {
                isAggro = true
                aggroTimerTask?.cancel()
                aggroTimerTask = aggroTimer.schedule(1000L * AGGRO_TIMER_SECONDS) {
                    isAggro = false
                    aggroTimerTask = null
                }

                return true
            }
        }

        return isAggro
    }

    override fun getIDs(): List<String> { return listOf() }

    companion object: MusicPredicateCompanion<CombatPredicate> {
        override fun getTypeName(): String { return "combat" }

        override fun fromJson(json: JsonObject, partialPath: String): CombatPredicate {
            return CombatPredicate(partialPath)
        }

        private val baseAxialDistance = Vec3d(10.0, 10.0, 10.0)
        private const val AGGRO_TIMER_SECONDS = 10L

        fun closeEnough(playerPos: BlockPos, attackerPos: BlockPos, attackerSize: Vec3d): Boolean
        {
            val displacement: BlockPos = playerPos.subtract(attackerPos)
            val axialDistance = Vec3i(abs(displacement.x), abs(displacement.y), abs(displacement.z))
            val scaledAttackerMinDistance = baseAxialDistance
                .multiply(Vec3d(cbrt(attackerSize.x), cbrt(attackerSize.y), cbrt(attackerSize.z)))
            val intScaledAttackerMinDistance = Vec3i(
                scaledAttackerMinDistance.x.toInt(),
                scaledAttackerMinDistance.y.toInt(),
                scaledAttackerMinDistance.z.toInt())
            return axialDistance.x < intScaledAttackerMinDistance.x
                    && axialDistance.y < intScaledAttackerMinDistance.y
                    && axialDistance.z < intScaledAttackerMinDistance.z
        }
    }
}