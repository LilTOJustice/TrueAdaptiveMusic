package liltojustice.trueadaptivemusic.client.mixin;

import liltojustice.trueadaptivemusic.common.client.TAMClient;
import net.minecraft.client.sounds.SoundEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SoundEngine.class)
public class SoundEngineMixin {
    @Inject(method = "destroy", at = @At("HEAD"))
    public void destroy(CallbackInfo ci) {
        TAMClient.INSTANCE.stop();
    }

    @Inject(method = "loadLibrary", at = @At("TAIL"))
    public void loadLibrary(CallbackInfo ci) {
        TAMClient.INSTANCE.initialize();
    }
}
