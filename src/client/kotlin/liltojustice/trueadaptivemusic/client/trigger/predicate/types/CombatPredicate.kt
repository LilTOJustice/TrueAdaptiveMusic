package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import liltojustice.trueadaptivemusic.client.identifier.EntityTypeIdentifier
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.Minecraft
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.monster.Guardian
import net.minecraft.world.entity.monster.Monster
import net.minecraft.world.entity.monster.Phantom
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

class CombatPredicate(
    private val blacklist: Boolean, private val mobEntities: List<EntityTypeIdentifier>) : MusicPredicate() {
    private val aggroTimer: Timer = Timer()
    private var aggroTimerTask: TimerTask? = null
    private var isAggro: Boolean = false
    private val entityTranslationKeys = mobEntities.map { mobEntity -> mobEntity.toLanguageKey("entity") }

    override fun test(): Boolean {
        val minecraft = Minecraft.getInstance()
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
            level.entitiesForRendering().filterIsInstance<Player>().filter { it != playerEntity && filterEntity(it) })

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

    private fun filterEntity(entity: Entity): Boolean {
        return entityTranslationKeys
            .takeIf { it.isNotEmpty() }
            ?.let {
                if (blacklist)
                    it.none { mobEntity -> mobEntity == entity.type.descriptionId }
                else
                    it.any { mobEntity -> mobEntity == entity.type.descriptionId }
            }
            ?: true
    }

    companion object: MusicPredicateCompanion {
        private val baseAxialDistance = Vec3(20.0, 20.0, 20.0)
        private const val AGGRO_TIMER_SECONDS = 4L
        private const val DEG_PER_RAD = 180.0 / PI

        override val argDescriptions: Map<String, String>
            get() = super.argDescriptions + mapOf(
                CombatPredicate::blacklist.name to "Whether the list of mob entities attacking should not (if " +
                        "checked) or should (if not checked) make the music play.",
                CombatPredicate::mobEntities.name to "Select mob entities for this predicate. If none, any entity " +
                        "will trigger the music."
            )

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
                            entity is Phantom||
                            (entity as? Player)?.let { isEnemyPlayer(playerEntity, it) } == true
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
}