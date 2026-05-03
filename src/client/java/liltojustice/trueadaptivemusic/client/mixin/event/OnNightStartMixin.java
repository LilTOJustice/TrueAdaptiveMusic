package liltojustice.trueadaptivemusic.client.mixin.event;

import liltojustice.trueadaptivemusic.client.trigger.event.types.OnNightStartEvent;
import liltojustice.trueadaptivemusicapi.TAMAPI;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class OnNightStartMixin {
    @Inject(at = @At("HEAD"), method = "tick()V")
    public void tick(CallbackInfo ci) {
        Minecraft thisObject = (Minecraft)(Object)this;
        if (thisObject.level != null && thisObject.level.getGameTime() % 24000L == 13000L) {
            TAMAPI.INSTANCE.invokeEvent(OnNightStartEvent.INSTANCE);
        }
    }
}