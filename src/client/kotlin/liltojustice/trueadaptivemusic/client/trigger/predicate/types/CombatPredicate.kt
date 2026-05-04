package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusicapi.identifier.EntityTypeIdentifier
import liltojustice.trueadaptivemusicapi.trigger.arguments.TriggerArguments
import liltojustice.trueadaptivemusicapi.trigger.predicate.type.PredicateType
import liltojustice.trueadaptivemusicapi.trigger.state.TriggerState
import net.minecraft.client.Minecraft
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.Mob
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
                    "will trigger the music."
        )

    override fun createState(arguments: Arguments): State {
        return State(arguments)
    }

    override fun test(arguments: Arguments, state: State): Boolean {
        val minecraft = Minecraft.getInstance()
        val playerEntity = minecraft.player ?: return false
        val level = minecraft.level ?: return false
        val verticalFov = minecraft.options.fov().get().toDouble() / DEG_PER_RAD
        val horizontalFov = 2 * atan(tan(verticalFov / 2) * minecraft.window.width / minecraft.window.height)
        val verticalAngle = acos(playerEntity.rotationVector.y)
        val horizontalAngle = acos(playerEntity.rotationVector.x)

        val entityGroups = mutableListOf<List<LivingEntity>>()

        entityGroups.add(level.entitiesForRendering().filterIsInstance<Monster>().filter { state.filterEntity(it) })
        entityGroups.add(level.entitiesForRendering().filterIsInstance<Phantom>().filter { state.filterEntity(it) })
        entityGroups.add(
            level.entitiesForRendering().filterIsInstance<Player>()
                .filter { it != playerEntity && state.filterEntity(it) }
        )

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
            playerEntity: Player,
            verticalAngle: Float,
            horizontalAngle: Float,
            verticalFov: Double,
            horizontalFov: Double
        ): Boolean {
            val relativeEntityPos = entity.position().subtract(playerEntity.position())
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

    private val baseAxialDistance = Vec3(20.0, 20.0, 20.0)
    private const val AGGRO_TIMER_SECONDS = 4L
    private const val DEG_PER_RAD = 180.0 / PI

    private fun isValidAttacker(entity: LivingEntity, playerEntity: Player, displacement: Vec3): Boolean {
        val closeEnough = closeEnough(
            displacement,
            Vec3(entity.boundingBox.xsize,
                entity.boundingBox.ysize,
                entity.boundingBox.zsize
            )
        )

        return closeEnough && (
                (entity as? Mob)?.isAggressive == true ||
                        (entity as? Guardian)?.let { it.target?.id == playerEntity.id } == true ||
                        entity is Phantom ||
                        (entity as? Player)?.let { isEnemyPlayer(playerEntity, it) } == true ||
                        entity is Warden
                )
    }

    private fun isEnemyPlayer(player: Player, otherPlayer: Player): Boolean {
        return player.team != null && otherPlayer.team != null && !player.isAlliedTo(otherPlayer)
    }

    private fun closeEnough(displacement: Vec3, attackerSize: Vec3): Boolean
    {
        val axialDistance = Vec3(
            abs(displacement.x), abs(displacement.y), abs(displacement.z))
        val scaledAttackerMinDistance = baseAxialDistance
            .multiply(
                Vec3(
                    cbrt(attackerSize.x), cbrt(attackerSize.y), cbrt(attackerSize.z))
            )

        return axialDistance.x < scaledAttackerMinDistance.x
                && axialDistance.y < scaledAttackerMinDistance.y
                && axialDistance.z < scaledAttackerMinDistance.z
    }
}