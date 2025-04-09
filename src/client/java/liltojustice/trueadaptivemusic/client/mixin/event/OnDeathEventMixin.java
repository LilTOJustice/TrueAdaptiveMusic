package liltojustice.trueadaptivemusic.client.mixin.event;

import liltojustice.trueadaptivemusic.client.Callbacks;
import liltojustice.trueadaptivemusic.client.event.types.OnDeathEvent;
import net.minecraft.client.gui.screen.DeathScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DeathScreen.class)
public class OnDeathEventMixin {
    @Inject(at = @At("HEAD"), method = "init()V")
    public void init(CallbackInfo ci) {
        Callbacks.Companion.invokeMusicEvent(OnDeathEvent.Companion.getTypeName());
    }
}