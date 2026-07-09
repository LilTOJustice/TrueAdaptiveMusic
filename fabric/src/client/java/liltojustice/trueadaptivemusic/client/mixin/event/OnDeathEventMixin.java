package liltojustice.trueadaptivemusic.client.mixin.event;

import liltojustice.trueadaptivemusic.common.client.trigger.event.types.OnDeathEvent;
import liltojustice.trueadaptivemusicapi.TAMAPI;
import net.minecraft.client.gui.screens.DeathScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DeathScreen.class)
public class OnDeathEventMixin {
    @Inject(at = @At("HEAD"), method = "init()V")
    public void init(CallbackInfo ci) {
        TAMAPI.INSTANCE.invokeEvent(OnDeathEvent.INSTANCE);
    }
}