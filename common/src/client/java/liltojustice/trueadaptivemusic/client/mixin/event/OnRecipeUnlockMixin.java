package liltojustice.trueadaptivemusic.client.mixin.event;

import liltojustice.trueadaptivemusic.client.trigger.event.types.OnRecipeUnlockEvent;
import liltojustice.trueadaptivemusicapi.TAMAPI;import net.minecraft.client.gui.components.toasts.*;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RecipeToast.class)
public class OnRecipeUnlockMixin {
    @Inject(at = @At("HEAD"), method = "addItem")
    public void addItem(RecipeHolder<?> p_301083_, CallbackInfo ci) {
        TAMAPI.INSTANCE.invokeEvent(
                OnRecipeUnlockEvent.INSTANCE,
                new OnRecipeUnlockEvent.Input(p_301083_.value().getToastSymbol().getItemHolder())
        );
    }
}