package liltojustice.trueadaptivemusic.client.mixin.event;

import liltojustice.trueadaptivemusic.client.TAMClient;
import liltojustice.trueadaptivemusic.client.trigger.event.MusicEvent;
import liltojustice.trueadaptivemusic.client.trigger.event.types.OnDayStartEvent;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class OnDayStartEventMixin {
    @Inject(at = @At("HEAD"), method = "tick()V")
    public void tick(CallbackInfo ci) {
        MinecraftClient thisObject = (MinecraftClient)(Object)this;
        if (thisObject.world != null && thisObject.world.getTimeOfDay() % 24000L == 0L) {
            MusicEvent.Companion.invokeMusicEvent(TAMClient.INSTANCE.getEventRegistry().get(OnDayStartEvent.class));
        }
    }
}