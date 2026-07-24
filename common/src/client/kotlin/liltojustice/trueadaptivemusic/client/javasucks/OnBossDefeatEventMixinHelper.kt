package liltojustice.trueadaptivemusic.client.javasucks

import liltojustice.trueadaptivemusic.client.trigger.event.types.OnBossDefeatEvent
import liltojustice.trueadaptivemusicapi.TAMAPI
import liltojustice.trueadaptivemusicapi.identifier.EntityIdentifier
import net.minecraft.client.Minecraft
import net.minecraft.world.entity.LivingEntity
import net.minecraft.network.chat.contents.TranslatableContents
import net.minecraft.resources.ResourceLocation

object OnBossDefeatEventMixinHelper {
    @JvmStatic
    fun onDeath(entity: LivingEntity) {
        if (!isBoss(entity)) {
            return
        }

        TAMAPI.invokeEvent(
            OnBossDefeatEvent,
            OnBossDefeatEvent.Input(
                EntityIdentifier(ResourceLocation.tryParse(entity.type.toString())!!)
            )
        )
    }

    private fun isBoss(entity: LivingEntity): Boolean {
        val client = Minecraft.getInstance()
        return client.gui.bossOverlay.events.values.any { bossBar ->
            val bossName = (bossBar.name.contents as? TranslatableContents)?.key ?: return@any false
            bossName == entity.type.descriptionId
        }
    }
}