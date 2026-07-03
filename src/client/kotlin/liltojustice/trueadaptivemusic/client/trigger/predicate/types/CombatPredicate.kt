package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusic.client.util.NInt
import liltojustice.trueadaptivemusicapi.identifier.EntityIdentifier
import liltojustice.trueadaptivemusicapi.trigger.arguments.TriggerArguments
import liltojustice.trueadaptivemusicapi.trigger.predicate.type.PredicateType
import liltojustice.trueadaptivemusicapi.trigger.state.TriggerState
import net.minecraft.client.Minecraft
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.monster.ElderGuardian
import net.minecraft.world.entity.monster.Guardian
import net.minecraft.world.entity.monster.Monster
import net.minecraft.world.entity.monster.Phantom
import net.minecraft.world.entity.monster.warden.Warden
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec3
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
                    "will trigger the music.",
            Arguments::minimumCount.name to "Select how many minimum mobs it takes to trigger the music. 0 counts a 1."
        )

    override fun createState(arguments: Arguments): State {
        return State(arguments)
    }

    override fun test(arguments: Arguments, state: State): Boolean {
        val minecraft = Minecraft.getInstance()
        return state.test(minecraft)
    }

    data class Arguments(
        val isBlacklist: Boolean = false,
        val entities: List<EntityIdentifier> = emptyList(),
        val minimumCount: NInt = NInt()
    ): TriggerArguments()

    class State(private val arguments: Arguments): TriggerState() {
        val aggroTimer: Timer = Timer()
        var aggroTimerTask: TimerTask? = null
        var isAggro: Boolean = false

        fun test(minecraft: Minecraft): Boolean {
            val playerEntity = minecraft.player ?: return false
            val level = minecraft.level ?: return false
            val verticalFov = minecraft.options.fov().get().toDouble() / DEG_PER_RAD
            val horizontalFov = 2 * atan(tan(verticalFov / 2) * minecraft.window.width / minecraft.window.height)
            val verticalAngle = acos(playerEntity.rotationVector.y)
            val horizontalAngle = acos(playerEntity.rotationVector.x)

            val entityGroups = mutableListOf<List<LivingEntity>>()

            entityGroups.add(level.entitiesForRendering().filterIsInstance<Monster>().filter { filterEntity(it) })
            entityGroups.add(level.entitiesForRendering().filterIsInstance<Phantom>().filter { filterEntity(it) })
            entityGroups.add(
                level.entitiesForRendering().filterIsInstance<Player>()
                    .filter { it != playerEntity && filterEntity(it) }
            )

            var count = 0U
            for (validEntities in entityGroups) {
                for (livingEntity: LivingEntity in validEntities) {
                    if (processEntity(
                            livingEntity, playerEntity, verticalAngle, horizontalAngle, verticalFov, horizontalFov)) {
                        count++
                    }
                }
            }

            if (count >= arguments.minimumCount.toUInt()) {
                isAggro = true
                aggroTimerTask?.cancel()
                aggroTimerTask = aggroTimer.schedule(1000L * AGGRO_TIMER_SECONDS) {
                    isAggro = false
                    aggroTimerTask = null
                }
            }

            return isAggro
        }

        fun processEntity(
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

            if ((abs(entityVerticalAngle - verticalAngle) > verticalFov / 2
                        || abs(entityHorizontalAngle - horizontalAngle) > horizontalFov / 2)) {
                return false
            }

            if (isValidAttacker(entity, playerEntity, relativeEntityPos)) {
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
            Vec3d(entity.boundingBox.lengthX,
                entity.boundingBox.lengthY,
                entity.boundingBox.lengthZ
            )
        )

        return closeEnough && (
                (entity as? Mob)?.isAggressive == true ||
                        (entity as? Guardian)?.let { it.activeAttackTarget?.id == playerEntity.id } == true ||
                        (entity as? ElderGuardian)?.let { it.activeAttackTarget?.id == playerEntity.id } == true ||
                        entity is Phantom ||
                        (entity as? Player)?.let { isEnemyPlayer(playerEntity, it) } == true ||
                        entity is Warden
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