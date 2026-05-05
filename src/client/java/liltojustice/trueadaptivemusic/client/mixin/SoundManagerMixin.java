package liltojustice.trueadaptivemusic.client.mixin;

import liltojustice.trueadaptivemusic.client.TAMClient;
import liltojustice.trueadaptivemusic.client.javasucks.SoundManagerMixinHelper;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.sound.SoundSystem;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SoundManager.class)
public class SoundManagerMixin {
    @Inject(method = "play(Lnet/minecraft/client/sound/SoundInstance;)Lnet/minecraft/client/sound/SoundSystem$PlayResult;", at = @At("HEAD"), cancellable = true)
    public void play(SoundInstance instance, CallbackInfoReturnable<SoundSystem.PlayResult> cir) {
        SoundManager thisObject = (SoundManager)(Object)this;

        if (instance.getCategory() == SoundCategory.MUSIC) {
            TAMClient.INSTANCE.setDesiredVanillaSoundEvent(SoundEvent.of(instance.getId()));
        }

        if (SoundManagerMixinHelper.shouldIgnore(instance)) {
            instance.getSoundSet(thisObject);
            cir.setReturnValue(SoundSystem.PlayResult.STARTED);
        }
    }

    @Inject(method = "updateSoundVolume", at = @At("HEAD"))
    public void updateSoundVolume(SoundCategory category, CallbackInfo ci) {
        TAMClient.INSTANCE.refreshSoundVolume();
    }
}
