package liltojustice.trueadaptivemusic.client.mixin;

import liltojustice.trueadaptivemusic.client.TAMClient;
import net.minecraft.client.sound.Channel;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.sound.Source;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SoundManager.class)
public class SoundManagerPauseAllMixin {
    @Inject(method = "pauseAll", at = @At("HEAD"), cancellable = true)
    public void stopAll(CallbackInfo ci) {
        System.out.println("STOP ALL CALLED");
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
}
