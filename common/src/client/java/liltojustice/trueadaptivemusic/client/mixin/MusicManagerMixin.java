package liltojustice.trueadaptivemusic.client.mixin;

import liltojustice.trueadaptivemusic.client.TAMClient;
import liltojustice.trueadaptivemusic.client.javasucks.MusicTrackerMixinHelper;
import net.minecraft.client.sounds.MusicInfo;
import net.minecraft.client.sounds.MusicManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MusicManager.class)
public class MusicManagerMixin {
    @Inject(method = "startPlaying", at = @At("HEAD"), cancellable = true)
    public void play(MusicInfo instance, CallbackInfo ci) {
        var music = instance.music();
        if (music == null) {
            return;
        }

        var sound = music.getEvent().value();
        TAMClient.INSTANCE.setDesiredVanillaSoundEvent(sound);
        if (MusicTrackerMixinHelper.shouldIgnore(music)) {
            ci.cancel();
        }
    }
}