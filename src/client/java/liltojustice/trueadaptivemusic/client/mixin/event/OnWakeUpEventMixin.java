package liltojustice.trueadaptivemusic.client.mixin.event;

import liltojustice.trueadaptivemusic.client.Callbacks;
import liltojustice.trueadaptivemusic.client.event.types.OnWakeUpEvent;
import net.minecraft.client.gui.screen.SleepingChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SleepingChatScreen.class)
public class OnWakeUpEventMixin {
    @Unique
    boolean stopSleepingPressed = false;

    @Inject(at = @At("HEAD"), method = "stopSleeping()V")
    public void stopSleeping(CallbackInfo ci) {
        stopSleepingPressed = true;
    }

    @Inject(at = @At("HEAD"), method = "closeChatIfEmpty()V")
    public void closeChatIfEmpty(CallbackInfo ci) {
        if (!stopSleepingPressed) {
            Callbacks.Companion.invokeMusicEvent(OnWakeUpEvent.Companion.getTypeName());
        }

        stopSleepingPressed = false;
    }
}