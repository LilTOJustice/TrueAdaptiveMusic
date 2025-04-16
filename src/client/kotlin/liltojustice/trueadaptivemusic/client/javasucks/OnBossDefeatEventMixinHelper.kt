package liltojustice.trueadaptivemusic.client.javasucks

import liltojustice.trueadaptivemusic.client.identifier.EntityTypeIdentifier
import liltojustice.trueadaptivemusic.client.trigger.event.MusicEvent
import liltojustice.trueadaptivemusic.client.trigger.event.types.OnBossDefeatEvent.Companion.getTypeName
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.LivingEntity
import net.minecraft.text.TranslatableTextContent

class OnBossDefeatEventMixinHelper {
    companion object {
        @JvmStatic
        fun onDeath(entity: LivingEntity) {
            if (isBoss(entity))
            {
                MusicEvent.invokeMusicEvent(
                    getTypeName(),
                    EntityTypeIdentifier(entity.type.toString())
                )
            }

        }

        private fun isBoss(entity: LivingEntity): Boolean {
            val client = MinecraftClient.getInstance()
            return client.inGameHud.bossBarHud.bossBars.values.any { bossBar ->
                val bossName = (bossBar.name.content as? TranslatableTextContent)?.key ?: return@any false
                bossName == entity.type.translationKey
            }
        }
    }
}