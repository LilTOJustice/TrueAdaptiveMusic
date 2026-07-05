package liltojustice.trueadaptivemusic.client.mixin.event;

import liltojustice.trueadaptivemusic.common.client.trigger.event.types.OnWakeUpEvent;
import liltojustice.trueadaptivemusicapi.TAMAPI;
import net.minecraft.client.gui.screens.InBedChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InBedChatScreen.class)
public class OnWakeUpEventMixin {
    @Unique
    boolean trueAdaptiveMusic$stopSleepingPressed = false;

    @Inject(at = @At("HEAD"), method = "onPlayerWokeUp")
    public void stopSleeping(CallbackInfo ci) {
        trueAdaptiveMusic$stopSleepingPressed = true;
    }

    @Inject(at = @At("HEAD"), method = "onClose")
    public void closeChatIfEmpty(CallbackInfo ci) {
        if (!trueAdaptiveMusic$stopSleepingPressed) {
            TAMAPI.INSTANCE.invokeEvent(OnWakeUpEvent.INSTANCE);
        }

        trueAdaptiveMusic$stopSleepingPressed = false;
    }
}