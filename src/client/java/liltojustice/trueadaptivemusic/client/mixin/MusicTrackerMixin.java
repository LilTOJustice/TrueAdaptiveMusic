package liltojustice.trueadaptivemusic.client.mixin;

import liltojustice.trueadaptivemusic.client.TAMClient;
import liltojustice.trueadaptivemusic.client.javasucks.MusicTrackerMixinHelper;
import liltojustice.trueadaptivemusic.client.sound.instance.AudioFileSoundInstance;
import net.minecraft.client.sound.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MusicTracker.class)
public class MusicTrackerMixin {
    @Inject(method = "play", at = @At("HEAD"), cancellable = true)
    public void play(MusicInstance instance, CallbackInfo ci) {
        var music = instance.music();
        if (music != null && MusicTrackerMixinHelper.shouldIgnore(music)) {
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
        if (TAMClient.INSTANCE.getMusicPack() != null) {
            ci.cancel();
        }
    }
}
