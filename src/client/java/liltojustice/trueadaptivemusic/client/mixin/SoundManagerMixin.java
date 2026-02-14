package liltojustice.trueadaptivemusic.client.mixin;

import liltojustice.trueadaptivemusic.client.TAMClient;
import liltojustice.trueadaptivemusic.client.javasucks.SoundManagerMixinHelper;
import net.minecraft.client.sound.*;
import net.minecraft.sound.SoundCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SoundManager.class)
public class SoundManagerMixin {
    @Inject(method = "play(Lnet/minecraft/client/sound/SoundInstance;)V", at = @At("HEAD"), cancellable = true)
    public void play(SoundInstance sound, CallbackInfo ci) {
        if (SoundManagerMixinHelper.shouldIgnore(sound)) {
            ci.cancel();
        }
    }

    @Inject(method = "updateSoundVolume", at = @At("HEAD"))
    public void updateSoundVolume(SoundCategory soundCategory, float f, CallbackInfo ci) {
        TAMClient.INSTANCE.refreshSoundVolume();
    }
}
