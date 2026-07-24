package liltojustice.trueadaptivemusic.client.mixin;

import liltojustice.trueadaptivemusic.client.TAMClient;
import net.minecraft.client.sounds.SoundEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SoundEngine.class)
public class SoundEngineMixin {
    @Inject(method = "loadLibrary", at = @At("TAIL"))
    public void init(CallbackInfo ci) {
        TAMClient.INSTANCE.initialize();
    }

    @Inject(method = "destroy()V", at = @At("HEAD"))
    public void destroy(CallbackInfo ci) {
        TAMClient.INSTANCE.stop();
    }
}
