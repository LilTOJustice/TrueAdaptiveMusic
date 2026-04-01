package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusic.client.identifier.EntityTypeIdentifier
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
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
    private val entityTranslationKeys = mobEntities.map { mobEntity -> mobEntity.toTranslationKey("entity") }

    override fun test(): Boolean {
        val client = MinecraftClient.getInstance()
        val playerEntity = client.player ?: return false
        val world = client.world ?: return false
        val verticalFov = client.options.fov.value.toDouble() / DEG_PER_RAD
        val horizontalFov = 2 * atan(tan(verticalFov / 2) * client.window.width / client.window.height)
        val verticalAngle = acos(playerEntity.rotationVecClient.y)
        val horizontalAngle = acos(playerEntity.rotationVecClient.x)

        val entityGroups = mutableListOf<List<LivingEntity>>()

        entityGroups.add(world.entities.filterIsInstance<HostileEntity>().filter { filterEntity(it) })
        entityGroups.add(world.entities.filterIsInstance<PhantomEntity>().filter { filterEntity(it) })
        entityGroups.add(
            world.entities.filterIsInstance<PlayerEntity>().filter { it != playerEntity && filterEntity(it) })

        for (validEntities in entityGroups) {
            for (livingEntity: LivingEntity in validEntities) {
                if (processEntity(
                        livingEntity, playerEntity, verticalAngle, horizontalAngle, verticalFov, horizontalFov)) {
                    return true
                }
            }
        }

        return isAggro
    }

    override fun getTickRate(): Int {
        return super.getTickRate() * 10
    }

    private fun processEntity(
        entity: LivingEntity,
        playerEntity: PlayerEntity,
        verticalAngle: Double,
        horizontalAngle: Double,
        verticalFov: Double,
        horizontalFov: Double
    ): Boolean {
        val relativeEntityPos = entity.entityPos.subtract(playerEntity.entityPos)
        val relativeEntityPosN = relativeEntityPos.normalize()

        val entityVerticalAngle = acos(relativeEntityPosN.y)
        val entityHorizontalAngle = acos(relativeEntityPosN.x)

        if (!isAggro && (abs(entityVerticalAngle - verticalAngle) > verticalFov / 2
                    || abs(entityHorizontalAngle - horizontalAngle) > horizontalFov / 2)) {
            return false
        }

        if (isValidAttacker(entity, playerEntity, relativeEntityPos)) {
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
        return entityTranslationKeys
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
                CombatPredicate::blacklist.name to "Whether the list of mob entities attacking should not (if " +
                        "checked) or should (if not checked) make the music play.",
                CombatPredicate::mobEntities.name to "Select mob entities for this predicate. If none, any entity " +
                        "will trigger the music."
            )

        private fun isValidAttacker(entity: LivingEntity, playerEntity: PlayerEntity, displacement: Vec3d): Boolean {
            val closeEnough = closeEnough(
                    displacement,
                    Vec3d(entity.boundingBox.lengthX,
                        entity.boundingBox.lengthY,
                        entity.boundingBox.lengthZ
                    )
            )

            return closeEnough && (
                    (entity as? MobEntity)?.isAttacking == true ||
                            (entity as? GuardianEntity)?.let { it.beamTarget?.id == playerEntity.id } == true ||
                            entity is PhantomEntity ||
                            (entity as? PlayerEntity)?.let { isEnemyPlayer(playerEntity, it) } == true
                    )
        }

        private fun isEnemyPlayer(player: PlayerEntity, otherPlayer: PlayerEntity): Boolean {
            return player.scoreboardTeam != null &&
                    otherPlayer.scoreboardTeam != null &&
                    !player.isTeammate(otherPlayer)
        }

        private fun closeEnough(displacement: Vec3d, attackerSize: Vec3d): Boolean
        {
            val axialDistance = Vec3d(
                abs(displacement.x), abs(displacement.y), abs(displacement.z))
            val scaledAttackerMinDistance = baseAxialDistance
                .multiply(
                    Vec3d(cbrt(attackerSize.x), cbrt(attackerSize.y), cbrt(attackerSize.z)))

            return axialDistance.x < scaledAttackerMinDistance.x
                    && axialDistance.y < scaledAttackerMinDistance.y
                    && axialDistance.z < scaledAttackerMinDistance.z
        }
    }
}