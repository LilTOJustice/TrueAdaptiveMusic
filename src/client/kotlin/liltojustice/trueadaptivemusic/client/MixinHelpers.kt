package liltojustice.trueadaptivemusic.client

import net.minecraft.client.MinecraftClient
import net.minecraft.entity.LivingEntity
import net.minecraft.text.TranslatableTextContent

class MixinHelpers {
    companion object {
        fun isBoss(entity: LivingEntity): Boolean {
            val client = MinecraftClient.getInstance()
            return client.inGameHud.bossBarHud.bossBars.values.any { bossBar ->
                val bossName = (bossBar.name.content as? TranslatableTextContent)?.key ?: return@any false
                bossName == entity.type.translationKey
            }
        }
    }
}