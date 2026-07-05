package liltojustice.trueadaptivemusic.client.mixin.event;

import liltojustice.trueadaptivemusic.common.client.javasucks.OnBossDefeatEventMixinHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class OnBossDefeatEventMixin {
    @Inject(at = @At("HEAD"), method = "die")
    public void onDeath(DamageSource source, CallbackInfo ci) {
        OnBossDefeatEventMixinHelper.onDeath((LivingEntity)(Object)this);
    }
}