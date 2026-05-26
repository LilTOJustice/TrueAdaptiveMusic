package liltojustice.trueadaptivemusic.client.mixin;

import liltojustice.trueadaptivemusic.client.TAMClient;
import liltojustice.trueadaptivemusic.client.javasucks.MusicTrackerMixinHelper;
import net.minecraft.client.sound.*;
import net.minecraft.sound.MusicSound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MusicTracker.class)
public class MusicTrackerMixin {
    @Inject(method = "play", at = @At("HEAD")   , cancellable = true)
    public void play(MusicSound type, CallbackInfo ci) {
        TAMClient.INSTANCE.setDesiredVanillaSoundEvent(type.getSound().value());
        if (MusicTrackerMixinHelper.shouldIgnore(type)) {
            ci.cancel();
        }
    }
}