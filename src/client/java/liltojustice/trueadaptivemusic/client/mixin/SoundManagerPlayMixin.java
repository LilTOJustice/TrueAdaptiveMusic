package liltojustice.trueadaptivemusic.client.mixin;

import liltojustice.trueadaptivemusic.client.TAMClient;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.sound.SoundCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SoundManager.class)
public class SoundManagerPlayMixin {
    @Inject(method = "play(Lnet/minecraft/client/sound/SoundInstance;)V", at = @At("HEAD"), cancellable = true)
    public void play(SoundInstance sound, CallbackInfo ci) {
        if (TAMClient.INSTANCE.getMusicPack() != null
                && sound.getCategory() == SoundCategory.MUSIC
                && !TAMClient.INSTANCE.hasSoundInstance(sound)) {
            ci.cancel();
        }
    }
}
