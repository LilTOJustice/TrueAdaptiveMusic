package liltojustice.trueadaptivemusic.client.mixin;

import liltojustice.trueadaptivemusic.client.TAMClient;
import liltojustice.trueadaptivemusic.client.javasucks.SoundManagerMixinHelper;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.sounds.SoundSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SoundManager.class)
public class SoundManagerMixin {
    @Inject(method = "play", at = @At("HEAD"), cancellable = true)
    public void play(SoundInstance instance, CallbackInfoReturnable<SoundEngine.PlayResult> cir) {
        if (SoundManagerMixinHelper.shouldIgnore(instance)) {
            cir.setReturnValue(SoundEngine.PlayResult.STARTED);
        }
    }

    @Inject(method = "refreshCategoryVolume", at = @At("HEAD"))
    public void refreshSoundVolumes(SoundSource category, CallbackInfo ci) {
        TAMClient.INSTANCE.refreshSoundVolume();
    }
}
