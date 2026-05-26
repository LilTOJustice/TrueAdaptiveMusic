package liltojustice.trueadaptivemusic.client.mixin;

import liltojustice.trueadaptivemusic.client.TAMClient;
import liltojustice.trueadaptivemusic.client.javasucks.MusicTrackerMixinHelper;
import net.minecraft.client.sound.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MusicTracker.class)
public class MusicTrackerMixin {
    @Inject(method = "play", at = @At("HEAD"), cancellable = true)
    public void play(MusicInstance instance, CallbackInfo ci) {
        var music = instance.music();
        if (music == null) {
            return;
        }

        var sound = music.getSound().value();
        TAMClient.INSTANCE.setDesiredVanillaSoundEvent(sound);
        if (MusicTrackerMixinHelper.shouldIgnore(music)) {
            ci.cancel();
        }
    }
}