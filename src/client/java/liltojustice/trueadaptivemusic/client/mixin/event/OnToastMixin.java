package liltojustice.trueadaptivemusic.client.mixin.event;

import liltojustice.trueadaptivemusic.client.Callbacks;
import liltojustice.trueadaptivemusic.client.event.types.OnAdvancementGetEvent;
import liltojustice.trueadaptivemusic.client.event.types.OnRecipeUnlockEvent;
import liltojustice.trueadaptivemusic.client.event.types.OnTutorialPopupEvent;
import net.minecraft.client.toast.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ToastManager.class)
public class OnToastMixin {
    @Inject(at = @At("HEAD"), method = "add(Lnet/minecraft/client/toast/Toast;)V")
    public void add(Toast toast, CallbackInfo ci) {
        if (toast instanceof AdvancementToast) {
            Callbacks.Companion.invokeMusicEvent(OnAdvancementGetEvent.Companion.getTypeName());
        }
        else if (toast instanceof RecipeToast) {
            Callbacks.Companion.invokeMusicEvent(OnRecipeUnlockEvent.Companion.getTypeName());
        }
        else if (toast instanceof TutorialToast) {
            Callbacks.Companion.invokeMusicEvent(OnTutorialPopupEvent.Companion.getTypeName());
        }
    }
}