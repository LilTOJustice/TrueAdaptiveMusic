package liltojustice.trueadaptivemusic.client.mixin.event;

import liltojustice.trueadaptivemusic.client.trigger.event.types.OnPauseEvent;
import liltojustice.trueadaptivemusicapi.TAMAPI;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class OnPauseMixin {
    @Inject(method = "openGameMenu", at = @At("HEAD"))
    public void openGameMenu(boolean suppressPauseMenuIfWeReallyArePausing, CallbackInfo ci) {
        if (MinecraftClient.getInstance().currentScreen == null) {
            TAMAPI.INSTANCE.invokeEvent(OnPauseEvent.INSTANCE);
        }
    }
}
