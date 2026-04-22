package liltojustice.trueadaptivemusic.client.mixin.event;

import liltojustice.trueadaptivemusic.client.TAMClient;
import liltojustice.trueadaptivemusic.client.trigger.event.types.OnWakeUpEvent;
import net.minecraft.client.gui.screens.InBedChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InBedChatScreen.class)
public class OnWakeUpEventMixin {
    @Unique
    boolean stopSleepingPressed = false;

    @Inject(at = @At("HEAD"), method = "onPlayerWokeUp")
    public void stopSleeping(CallbackInfo ci) {
        stopSleepingPressed = true;
    }

    @Inject(at = @At("HEAD"), method = "onClose")
    public void closeChatIfEmpty(CallbackInfo ci) {
        if (!stopSleepingPressed) {
            TAMClient.INSTANCE.invokeMusicEvent(OnWakeUpEvent.INSTANCE);
        }

        stopSleepingPressed = false;
    }
}