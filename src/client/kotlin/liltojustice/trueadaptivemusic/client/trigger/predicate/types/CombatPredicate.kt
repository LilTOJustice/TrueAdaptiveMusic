package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusic.client.identifier.EntityTypeIdentifier
import liltojustice.trueadaptivemusicapi.trigger.arguments.TriggerArguments
import liltojustice.trueadaptivemusicapi.trigger.predicate.type.PredicateType
import liltojustice.trueadaptivemusicapi.trigger.state.TriggerState
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.mob.GuardianEntity
import net.minecraft.entity.mob.HostileEntity
import net.minecraft.entity.mob.MobEntity
import net.minecraft.entity.mob.PhantomEntity
import net.minecraft.entity.mob.WardenEntity
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
import kotlin.reflect.typeOf

object CombatPredicate: PredicateType<CombatPredicate.Arguments, CombatPredicate.State>(
    "combat", typeOf<Arguments>()
) {
    override val tickRate: Int
        get() = super.tickRate * 10
    override val argDescriptions: Map<String, String>
        get() = super.argDescriptions + mapOf(
            Arguments::isBlacklist.name to "Whether the list of mob entities attacking should not (if " +
                    "checked) or should (if not checked) make the music play.",
            Arguments::entities.name to "Select mob entities for this predicate. If none, any entity " +
                    "will trigger the music."
        )

    override fun createState(arguments: Arguments): State {
        return State(arguments)
    }

    override fun test(arguments: Arguments, state: State): Boolean {
        val minecraft = MinecraftClient.getInstance()
        val playerEntity = minecraft.player ?: return false
        val level = minecraft.world ?: return false
        val verticalFov = minecraft.options.fov.value.toDouble() / DEG_PER_RAD
        val horizontalFov = 2 * atan(tan(verticalFov / 2) * minecraft.window.width / minecraft.window.height)
        val verticalAngle = acos(playerEntity.rotationVecClient.y)
        val horizontalAngle = acos(playerEntity.rotationVecClient.x)

        val entityGroups = mutableListOf<List<LivingEntity>>()

        entityGroups.add(level.entities.filterIsInstance<HostileEntity>().filter { state.filterEntity(it) })
        entityGroups.add(level.entities.filterIsInstance<PhantomEntity>().filter { state.filterEntity(it) })
        entityGroups.add(
            level.entities.filterIsInstance<PlayerEntity>().filter { it != playerEntity && state.filterEntity(it) })

        for (validEntities in entityGroups) {
            for (livingEntity: LivingEntity in validEntities) {
                if (state.processEntity(
                        livingEntity, playerEntity, verticalAngle, horizontalAngle, verticalFov, horizontalFov)) {
                    return true
                }
            }
        }

        return state.isAggro
    }

    data class Arguments(val isBlacklist: Boolean, val entities: List<EntityTypeIdentifier>): TriggerArguments()

    class State(private val arguments: Arguments): TriggerState() {
        val aggroTimer: Timer = Timer()
        var aggroTimerTask: TimerTask? = null
        var isAggro: Boolean = false

        fun processEntity(
            entity: LivingEntity,
            playerEntity: PlayerEntity,
            verticalAngle: Double,
            horizontalAngle: Double,
            verticalFov: Double,
            horizontalFov: Double
        ): Boolean {
            val relativeEntityPos = entity.pos.subtract(playerEntity.pos)
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

        fun filterEntity(entity: Entity): Boolean {
            return arguments.entities
                .takeIf { it.isNotEmpty() }
                ?.let {
                    if (arguments.isBlacklist)
                        it.none { mobEntity -> mobEntity.matches(entity) }
                    else
                        it.any { mobEntity -> mobEntity.matches(entity) }
                }
                ?: true
        }
    }

    private val baseAxialDistance = Vec3d(20.0, 20.0, 20.0)
    private const val AGGRO_TIMER_SECONDS = 4L
    private const val DEG_PER_RAD = 180.0 / PI

    private fun isValidAttacker(entity: LivingEntity, playerEntity: PlayerEntity, displacement: Vec3d): Boolean {
        val closeEnough = closeEnough(
            displacement,
            Vec3d(entity.boundingBox.xLength,
                entity.boundingBox.yLength,
                entity.boundingBox.zLength
            )
        )

        return closeEnough && (
                (entity as? MobEntity)?.isAttacking == true ||
                        (entity as? GuardianEntity)?.let { it.target?.id == playerEntity.id } == true ||
                        entity is PhantomEntity ||
                        (entity as? PlayerEntity)?.let { isEnemyPlayer(playerEntity, it) } == true ||
                        entity is WardenEntity
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