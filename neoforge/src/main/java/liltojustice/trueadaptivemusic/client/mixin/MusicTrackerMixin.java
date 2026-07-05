package liltojustice.trueadaptivemusic.client.mixin;

import liltojustice.trueadaptivemusic.common.client.TAMClient;
import liltojustice.trueadaptivemusic.common.client.javasucks.MusicTrackerMixinHelper;
import net.minecraft.client.sounds.MusicManager;
import net.minecraft.sounds.Music;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MusicManager.class)
public class MusicTrackerMixin {
    @Inject(method = "startPlaying", at = @At("HEAD"), cancellable = true)
    public void play(Music music, CallbackInfo ci) {
        TAMClient.INSTANCE.setDesiredVanillaSoundEvent(music.sound().value());
        if (MusicTrackerMixinHelper.shouldIgnore(music)) {
            ci.cancel();
        }
    }

    @Inject(method = "getCurrentMusicTranslationKey", at = @At("HEAD"), cancellable = true)
    public void getCurrentMusicTranslationKey(CallbackInfoReturnable<String> cir) {
        var currentTAMMusic = TAMClient.INSTANCE.getCurrentMusic();
        if (currentTAMMusic != null) {
            cir.setReturnValue(currentTAMMusic.getSoundString());
        }
    }
}
