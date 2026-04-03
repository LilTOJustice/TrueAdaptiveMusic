package liltojustice.trueadaptivemusic.client.javasucks

import liltojustice.trueadaptivemusic.client.TAMClient
import liltojustice.trueadaptivemusic.client.identifier.EntityTypeIdentifier
import liltojustice.trueadaptivemusic.client.trigger.event.types.OnBossDefeatEvent
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.contents.TranslatableContents
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.LivingEntity

object OnBossDefeatEventMixinHelper {
    @JvmStatic
    fun onDeath(entity: LivingEntity) {
        if (!isBoss(entity)) {
            return
        }

        TAMClient.invokeMusicEvent(
            OnBossDefeatEvent::class,
            EntityTypeIdentifier(Identifier.parse(entity.type.toString()))
        )
    }

    private fun isBoss(entity: LivingEntity): Boolean {
        val minecraft = Minecraft.getInstance()
        return minecraft.gui.bossOverlay.events.values.any { bossBar ->
            val bossName = (bossBar.name.contents as? TranslatableContents)?.key ?: return@any false
            bossName == entity.type.descriptionId
        }
    }
}