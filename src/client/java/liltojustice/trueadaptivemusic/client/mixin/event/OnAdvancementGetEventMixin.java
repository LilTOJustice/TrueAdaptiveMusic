package liltojustice.trueadaptivemusic.client.mixin.event;

import liltojustice.trueadaptivemusic.client.Callbacks;
import liltojustice.trueadaptivemusic.client.event.types.OnAdvancementGetEvent;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ToastManager.class)
public class OnAdvancementGetEventMixin {
    @Inject(at = @At("HEAD"), method = "add(Lnet/minecraft/client/toast/Toast;)V")
    public void add(Toast toast, CallbackInfo ci) {
        Callbacks.Companion.invokeMusicEvent(OnAdvancementGetEvent.Companion.getTypeName());
    }
}