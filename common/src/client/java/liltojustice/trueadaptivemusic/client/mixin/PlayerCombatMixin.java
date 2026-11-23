package liltojustice.trueadaptivemusic.client.mixin;

import liltojustice.trueadaptivemusic.client.cache.TAMClientCache;
import net.minecraft.client.Minecraft;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class PlayerCombatMixin {
    @Inject(method = "hurtOrSimulate", at = @At("HEAD"))
    public void hurtClient(DamageSource source, float damage, CallbackInfoReturnable<Boolean> cir) {
        var attackee = (Entity)(Object)this;
        var attackerEntity = source.getEntity();
        if (!(attackerEntity instanceof Player attackerPlayer)) {
            return;
        }

        var actualPlayer = Minecraft.getInstance().player;
        if (actualPlayer == null) {
            return;
        }

        if (attackerPlayer.getId() != actualPlayer.getId()) {
            TAMClientCache.INSTANCE.getPlayerCombatantTracker().hitBy(attackerPlayer, actualPlayer.tickCount);
        }
        else if (attackee instanceof Player) {
            TAMClientCache.INSTANCE.getPlayerCombatantTracker().hit((Player)attackee, actualPlayer.tickCount);
        }
    }
}
