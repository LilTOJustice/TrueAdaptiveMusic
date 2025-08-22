package liltojustice.trueadaptivemusic.client.mixin.event;

import liltojustice.trueadaptivemusic.client.trigger.event.MusicEvent;
import liltojustice.trueadaptivemusic.client.trigger.event.types.OnAdvancementGetEvent;
import liltojustice.trueadaptivemusic.client.trigger.event.types.OnRecipeUnlockEvent;
import liltojustice.trueadaptivemusic.client.trigger.event.types.OnTutorialPopupEvent;
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
            MusicEvent.Companion.invokeMusicEvent(MusicEvent.Companion.getNameFromType(OnAdvancementGetEvent.class));
        }
        else if (toast instanceof RecipeToast) {
            MusicEvent.Companion.invokeMusicEvent(MusicEvent.Companion.getNameFromType(OnRecipeUnlockEvent.class));
        }
        else if (toast instanceof TutorialToast) {
            MusicEvent.Companion.invokeMusicEvent(MusicEvent.Companion.getNameFromType(OnTutorialPopupEvent.class));
        }
    }
}