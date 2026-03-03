package liltojustice.trueadaptivemusic.client.javasucks

import liltojustice.trueadaptivemusic.client.TAMClient
import liltojustice.trueadaptivemusic.client.identifier.EntityTypeIdentifier
import liltojustice.trueadaptivemusic.client.trigger.event.types.OnBossDefeatEvent
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.LivingEntity
import net.minecraft.text.TranslatableTextContent
import net.minecraft.util.Identifier

object OnBossDefeatEventMixinHelper {
    @JvmStatic
    fun onDeath(entity: LivingEntity) {
        if (!isBoss(entity)) {
            return
        }

        TAMClient.invokeMusicEvent(
            OnBossDefeatEvent::class,
            EntityTypeIdentifier(Identifier.of(entity.type.toString()))
        )
    }

    private fun isBoss(entity: LivingEntity): Boolean {
        val client = MinecraftClient.getInstance()
        return client.inGameHud.bossBarHud.bossBars.values.any { bossBar ->
            val bossName = (bossBar.name.content as? TranslatableTextContent)?.key ?: return@any false
            bossName == entity.type.translationKey
        }
    }
}