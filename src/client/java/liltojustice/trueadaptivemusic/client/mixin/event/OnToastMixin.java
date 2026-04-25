package liltojustice.trueadaptivemusic.client.mixin.event;

import liltojustice.trueadaptivemusic.client.TAMClient;
import liltojustice.trueadaptivemusic.client.trigger.event.types.OnAdvancementGetEvent;
import liltojustice.trueadaptivemusic.client.trigger.event.types.OnRecipeUnlockEvent;
import liltojustice.trueadaptivemusic.client.trigger.event.types.OnTutorialPopupEvent;
import net.minecraft.client.gui.components.toasts.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ToastManager.class)
public class OnToastMixin {
    @Inject(at = @At("HEAD"), method = "addToast")
    public void add(Toast toast, CallbackInfo ci) {
        if (toast instanceof AdvancementToast) {
            TAMClient.INSTANCE.invokeMusicEvent(OnAdvancementGetEvent.INSTANCE);
        }
        else if (toast instanceof RecipeToast) {
            TAMClient.INSTANCE.invokeMusicEvent(OnRecipeUnlockEvent.INSTANCE);
        }
        else if (toast instanceof TutorialToast) {
            TAMClient.INSTANCE.invokeMusicEvent(OnTutorialPopupEvent.INSTANCE);
        }
    }
}