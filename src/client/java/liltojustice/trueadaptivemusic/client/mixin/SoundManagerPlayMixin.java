package liltojustice.trueadaptivemusic.client.mixin;

import liltojustice.trueadaptivemusic.Constants;
import liltojustice.trueadaptivemusic.client.Callbacks;
import liltojustice.trueadaptivemusic.client.MusicPack;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.sound.SoundCategory;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SoundManager.class)
public class SoundManagerPlayMixin {
    @Inject(method = "play(Lnet/minecraft/client/sound/SoundInstance;)V", at = @At("HEAD"), cancellable = true)
    public void play(SoundInstance sound, CallbackInfo ci) {
        @Nullable MusicPack musicPack = Callbacks.Companion.getCurrentMusicPack();
        if (musicPack != null
                && sound.getCategory() == SoundCategory.MUSIC
                && sound.getId() != Constants.Companion.getTRUEADAPTIVEMUSIC_ID()
        ) {
            ci.cancel();
        }
    }
}
