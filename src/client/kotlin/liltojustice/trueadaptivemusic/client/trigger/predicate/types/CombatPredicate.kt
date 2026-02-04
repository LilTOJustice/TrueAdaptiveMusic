package liltojustice.trueadaptivemusic.client.trigger.predicate.types

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import liltojustice.trueadaptivemusic.client.identifier.EntityTypeIdentifier
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.mob.GuardianEntity
import net.minecraft.entity.mob.HostileEntity
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
    private val blacklist: Boolean, private val mobEntities: List<EntityTypeIdentifier>): MusicPredicate() {
    private val aggroTimer: Timer = Timer()
    private var aggroTimerTask: TimerTask? = null
    private var isAggro: Boolean = false
    private val mobEntityTranslationKeys = mobEntities.map { mobEntity -> mobEntity.toTranslationKey("entity") }

    override fun test(client: MinecraftClient): Boolean {
        val playerEntity = client.player ?: return false
        val world = client.world ?: return false
        val verticalFov = client.options.fov.value.toDouble() / DEG_PER_RAD
        val horizontalFov = 2 * atan(tan(verticalFov / 2) * client.window.width / client.window.height)
        val verticalAngle = acos(playerEntity.rotationVecClient.y)
        val horizontalAngle = acos(playerEntity.rotationVecClient.x)

        val validEntities = world.entities
            .mapNotNull { it as? HostileEntity }
            .filter { entity ->
                mobEntityTranslationKeys
                    .takeIf { it.isNotEmpty() }
                    ?.let {
                        if (blacklist)
                            it.none { mobEntity -> mobEntity == entity.type.translationKey }
                        else
                            it.any { mobEntity -> mobEntity == entity.type.translationKey }
                    }
                    ?: true }

        for (mobEntity: HostileEntity in validEntities)
        {
            val relativeMobEntityPosN = mobEntity.entityPos.subtract(playerEntity.entityPos).normalize()

            val mobVerticalAngle = acos(relativeMobEntityPosN.y)
            val mobHorizontalAngle = acos(relativeMobEntityPosN.x)

            if (!isAggro && (abs(mobVerticalAngle - verticalAngle) > verticalFov / 2
                        || abs(mobHorizontalAngle - horizontalAngle) > horizontalFov / 2)) {
                continue
            }

            if (isValidAttacker(mobEntity, playerEntity))
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

    override fun getTickRate(): Int {
        return super.getTickRate() * 2
    }

    override fun toJson(): JsonObject {
        val result = JsonObject()
        result.addProperty("blacklist", blacklist)
        val mobEntities = JsonArray()
        this.mobEntities.forEach { mobEntity -> mobEntities.add(mobEntity.toString()) }
        result.add("mobEntities", mobEntities)

        return result
    }

    companion object: MusicPredicateCompanion<CombatPredicate> {
        private val baseAxialDistance = Vec3d(20.0, 20.0, 20.0)
        private const val AGGRO_TIMER_SECONDS = 2L
        private const val DEG_PER_RAD = 180.0 / PI

        override fun fromJson(json: JsonObject): CombatPredicate {
            return CombatPredicate(
                if (json.has("blacklist")) {
                    json.getAsJsonPrimitive("blacklist").asBoolean
                }
                else {
                    false
                },
                if (json.has("mobEntities")) {
                    json.getAsJsonArray("mobEntities").map {
                            element -> EntityTypeIdentifier(element.asString) }
                }
                else {
                    listOf()
                }
            )
        }

        fun closeEnough(displacement: Vec3d, attackerSize: Vec3d): Boolean
        {
            val axialDistance = Vec3d(abs(displacement.x), abs(displacement.y), abs(displacement.z))
            val scaledAttackerMinDistance = baseAxialDistance
                .multiply(Vec3d(cbrt(attackerSize.x), cbrt(attackerSize.y), cbrt(attackerSize.z)))
            return axialDistance.x < scaledAttackerMinDistance.x
                    && axialDistance.y < scaledAttackerMinDistance.y
                    && axialDistance.z < scaledAttackerMinDistance.z
        }
        private fun isValidAttacker(mobEntity: HostileEntity, playerEntity: PlayerEntity): Boolean {
            return (mobEntity.isAttacking && closeEnough(
                    relativeMobEntityPosN,
                    Vec3d(mobEntity.boundingBox.lengthX,
                        mobEntity.boundingBox.lengthY,
                        mobEntity.boundingBox.lengthZ))) ||
                    ((mobEntity as? GuardianEntity)?.let { it.beamTarget?.id == playerEntity.id } == true )
        }
    }
}