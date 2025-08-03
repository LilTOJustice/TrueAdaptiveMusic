package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.Entity
import net.minecraft.entity.mob.MobEntity
import net.minecraft.util.math.Vec3d
import java.util.*
import kotlin.concurrent.schedule
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.atan
import kotlin.math.cbrt
import kotlin.math.tan

class CombatPredicate: MusicPredicate() {
    private val aggroTimer: Timer = Timer()
    private var aggroTimerTask: TimerTask? = null
    private var isAggro: Boolean = false

    override fun test(client: MinecraftClient): Boolean {
        val playerEntity = client.player ?: return false
        val world = client.world ?: return false
        val verticalFov = client.options.fov.value.toDouble() / DEG_PER_RAD
        val horizontalFov = 2 * atan(tan(verticalFov / 2) * client.window.width / client.window.height)
        val verticalAngle = acos(playerEntity.rotationVecClient.y)
        val horizontalAngle = acos(playerEntity.rotationVecClient.x)

        for (entity: Entity? in world.entities)
        {
            val mobEntity: MobEntity = entity as? MobEntity ?: continue
            val relativeMobEntityPosN = mobEntity.pos.subtract(playerEntity.pos).normalize()

            val mobVerticalAngle = acos(relativeMobEntityPosN.y)
            val mobHorizontalAngle = acos(relativeMobEntityPosN.x)

            if (!isAggro && (abs(mobVerticalAngle - verticalAngle) > verticalFov / 2
                        || abs(mobHorizontalAngle - horizontalAngle) > horizontalFov / 2)) {
                continue
            }

            if (mobEntity.attacking?.id == playerEntity.id
                || (mobEntity.isAttacking
                        && closeEnough(
                    relativeMobEntityPosN,
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

    companion object: MusicPredicateCompanion<CombatPredicate> {
        override fun getTypeName(): String { return "combat" }

        override fun fromJson(json: JsonObject): CombatPredicate {
            return CombatPredicate()
        }

        private val baseAxialDistance = Vec3d(20.0, 20.0, 20.0)
        private const val AGGRO_TIMER_SECONDS = 2L
        private const val DEG_PER_RAD = 180.0 / PI

        fun closeEnough(displacement: Vec3d, attackerSize: Vec3d): Boolean
        {
            val axialDistance = Vec3d(abs(displacement.x), abs(displacement.y), abs(displacement.z))
            val scaledAttackerMinDistance = baseAxialDistance
                .multiply(Vec3d(cbrt(attackerSize.x), cbrt(attackerSize.y), cbrt(attackerSize.z)))
            return axialDistance.x < scaledAttackerMinDistance.x
                    && axialDistance.y < scaledAttackerMinDistance.y
                    && axialDistance.z < scaledAttackerMinDistance.z
        }
    }
}