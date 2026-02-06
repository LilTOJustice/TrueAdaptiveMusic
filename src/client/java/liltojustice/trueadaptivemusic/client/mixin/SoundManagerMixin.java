package liltojustice.trueadaptivemusic.client.mixin;

import liltojustice.trueadaptivemusic.client.TAMClient;
import liltojustice.trueadaptivemusic.client.javasucks.SoundManagerMixinHelper;
import net.minecraft.client.sound.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SoundManager.class)
public class SoundManagerMixin {
    @Inject(method = "play(Lnet/minecraft/client/sound/SoundInstance;)Lnet/minecraft/client/sound/SoundSystem$PlayResult;", at = @At("HEAD"), cancellable = true)
    public void play(SoundInstance sound, CallbackInfoReturnable<SoundSystem.PlayResult> cir) {
        if (SoundManagerMixinHelper.shouldIgnore(sound)) {
            cir.setReturnValue(SoundSystem.PlayResult.STARTED);
        }
    }

    @Inject(method = "pauseAll", at = @At("HEAD"), cancellable = true)
    public void pauseAllExcept(CallbackInfo ci) {
        SoundManager thisObject = (SoundManager)(Object)this;
        thisObject.soundSystem.sources.keySet().forEach(instance ->
        {
            if (!TAMClient.INSTANCE.hasSoundInstance(instance)) {
                Channel.SourceManager source = thisObject.soundSystem.sources.get(instance);
                if (source != null) {
                    source.run(Source::pause);
                }
            }
        });
        ci.cancel();
    }

    @Inject(method = "stopAll", at = @At("HEAD"))
    public void stopAll(CallbackInfo ci) {
        TAMClient.INSTANCE.resetCache();
    }
}
