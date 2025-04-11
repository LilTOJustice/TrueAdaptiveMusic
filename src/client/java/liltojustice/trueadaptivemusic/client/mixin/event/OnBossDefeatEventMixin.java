package liltojustice.trueadaptivemusic.client.mixin.event;

import liltojustice.trueadaptivemusic.client.Callbacks;
import liltojustice.trueadaptivemusic.client.MixinHelpers;
import liltojustice.trueadaptivemusic.client.event.types.OnBossDefeatEvent;
import liltojustice.trueadaptivemusic.client.identifier.EntityTypeIdentifier;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class OnBossDefeatEventMixin {
    @Inject(at = @At("HEAD"), method = "onDeath(Lnet/minecraft/entity/damage/DamageSource;)V")
    public void onDeath(DamageSource damageSource, CallbackInfo ci) {
        LivingEntity thisObject = (LivingEntity)(Object)this;
        if (MixinHelpers.Companion.isBoss(thisObject)) {
            Callbacks.Companion.invokeMusicEvent(
                    OnBossDefeatEvent.Companion.getTypeName(),
                    new EntityTypeIdentifier(thisObject.getType().toString()));
        }
    }
}