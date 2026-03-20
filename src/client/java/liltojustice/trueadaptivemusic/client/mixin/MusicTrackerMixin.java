package liltojustice.trueadaptivemusic.client.mixin;

import liltojustice.trueadaptivemusic.client.TAMClient;
import liltojustice.trueadaptivemusic.client.javasucks.MusicTrackerMixinHelper;
import liltojustice.trueadaptivemusic.client.sound.instance.AudioFileSoundInstance;
import net.minecraft.client.sound.*;
import net.minecraft.sound.MusicSound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MusicTracker.class)
public class MusicTrackerMixin {
    @Inject(method = "play", at = @At("HEAD"), cancellable = true)
    public void play(MusicSound sound, CallbackInfo ci) {
        if (MusicTrackerMixinHelper.shouldIgnore(sound)) {
            ci.cancel();
        }
    }

    @Inject(method = "getCurrentMusicTranslationKey", at = @At("HEAD"), cancellable = true)
    public void getCurrentMusicTranslationKey(CallbackInfoReturnable<String> cir) {
        MusicTracker thisObject = (MusicTracker)(Object)this;
        if (thisObject.current instanceof AudioFileSoundInstance sound) {
            cir.setReturnValue(sound.getFileName());
        }
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    public void tick(CallbackInfo ci) {
       var result = TAMClient.INSTANCE.getCurrentPredicateResult();
        if (TAMClient.INSTANCE.getMusicPack() != null &&
                result != null &&
                !result.getParameters().getVanillaBehavior()
        ) {
            ci.cancel();
        }
    }
}
