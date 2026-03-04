package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusic.client.identifier.EntityTypeIdentifier
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.Entity
import net.minecraft.entity.mob.GuardianEntity
import net.minecraft.entity.mob.HostileEntity
import net.minecraft.entity.mob.MobEntity
import net.minecraft.entity.mob.PhantomEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.Vec3d
import java.util.*
import kotlin.concurrent.schedule
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.atan
import kotlin.math.cbrt
import kotlin.math.tan

class CombatPredicate(
    private val blacklist: Boolean, private val mobEntities: List<EntityTypeIdentifier>) : MusicPredicate() {
    private val aggroTimer: Timer = Timer()
    private var aggroTimerTask: TimerTask? = null
    private var isAggro: Boolean = false
    private val mobEntityTranslationKeys = mobEntities.map { mobEntity -> mobEntity.toTranslationKey("entity") }

    override fun test(): Boolean {
        val client = MinecraftClient.getInstance()
        val playerEntity = client.player ?: return false
        val world = client.world ?: return false
        val verticalFov = client.options.fov.value.toDouble() / DEG_PER_RAD
        val horizontalFov = 2 * atan(tan(verticalFov / 2) * client.window.width / client.window.height)
        val verticalAngle = acos(playerEntity.rotationVecClient.y)
        val horizontalAngle = acos(playerEntity.rotationVecClient.x)

        val entityGroups = mutableListOf<List<MobEntity>>()

        entityGroups.add(world.entities.mapNotNull { it as? HostileEntity }.filter { filterEntity(it) })
        entityGroups.add(world.entities.mapNotNull { it as? PhantomEntity }.filter { filterEntity(it) })

        for (validEntities in entityGroups) {
            for (mobEntity: MobEntity in validEntities) {
                if (processMob(mobEntity, playerEntity, verticalAngle, horizontalAngle, verticalFov, horizontalFov)) {
                    return true
                }
            }
        }

        return isAggro
    }

    override fun getTickRate(): Int {
        return super.getTickRate() * 2
    }

    private fun processMob(mobEntity: MobEntity, playerEntity: PlayerEntity, verticalAngle: Double, horizontalAngle: Double, verticalFov: Double, horizontalFov: Double): Boolean {
        val relativeMobEntityPos = mobEntity.pos.subtract(playerEntity.pos)
        val relativeMobEntityPosN = relativeMobEntityPos.normalize()

        val mobVerticalAngle = acos(relativeMobEntityPosN.y)
        val mobHorizontalAngle = acos(relativeMobEntityPosN.x)

        if (!isAggro && (abs(mobVerticalAngle - verticalAngle) > verticalFov / 2
                    || abs(mobHorizontalAngle - horizontalAngle) > horizontalFov / 2)) {
            return false
        }

        if (isValidAttacker(mobEntity, playerEntity, relativeMobEntityPos)) {
            isAggro = true
            aggroTimerTask?.cancel()
            aggroTimerTask = aggroTimer.schedule(1000L * AGGRO_TIMER_SECONDS) {
                isAggro = false
                aggroTimerTask = null
            }

            return true
        }

        return false
    }

    private fun filterEntity(entity: Entity): Boolean {
        return mobEntityTranslationKeys
            .takeIf { it.isNotEmpty() }
            ?.let {
                if (blacklist)
                    it.none { mobEntity -> mobEntity == entity.type.translationKey }
                else
                    it.any { mobEntity -> mobEntity == entity.type.translationKey }
            }
            ?: true
    }

    companion object: MusicPredicateCompanion {
        private val baseAxialDistance = Vec3d(20.0, 20.0, 20.0)
        private const val AGGRO_TIMER_SECONDS = 4L
        private const val DEG_PER_RAD = 180.0 / PI

        override val argDescriptions: Map<String, String>
            get() = super.argDescriptions + mapOf(
                "blacklist" to "Whether the list of mob entities attacking should not (if checked) or should " +
                        "(if not checked) make the music play.",
                "mobEntities" to "Select mob entities for this predicate. If none, any entity will trigger the music."
            )

        private fun isValidAttacker(
            mobEntity: MobEntity, playerEntity: PlayerEntity, displacement: Vec3d): Boolean {
            val closeEnough = closeEnough(
                    displacement,
                    Vec3d(mobEntity.boundingBox.lengthX,
                        mobEntity.boundingBox.lengthY,
                        mobEntity.boundingBox.lengthZ
                    )
            )
            return (mobEntity.isAttacking && closeEnough) ||
                    ((mobEntity as? GuardianEntity)?.let { it.beamTarget?.id == playerEntity.id } == true) ||
                    ((mobEntity is PhantomEntity) && closeEnough)
        }

        private fun closeEnough(displacement: Vec3d, attackerSize: Vec3d): Boolean
        {
            val axialDistance = Vec3d(
                abs(displacement.x), abs(displacement.y), abs(displacement.z))
            val scaledAttackerMinDistance = baseAxialDistance
                .multiply(Vec3d(cbrt(attackerSize.x), cbrt(attackerSize.y), cbrt(attackerSize.z)))
            return axialDistance.x < scaledAttackerMinDistance.x
                    && axialDistance.y < scaledAttackerMinDistance.y
                    && axialDistance.z < scaledAttackerMinDistance.z
        }
    }
}