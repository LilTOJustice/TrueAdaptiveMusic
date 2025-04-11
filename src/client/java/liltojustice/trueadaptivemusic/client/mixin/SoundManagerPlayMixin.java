package liltojustice.trueadaptivemusic.client.mixin;

import liltojustice.trueadaptivemusic.Constants;
import liltojustice.trueadaptivemusic.client.Callbacks;
import liltojustice.trueadaptivemusic.client.MusicManager;
import liltojustice.trueadaptivemusic.client.event.types.OnAdvancementGetEvent;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SoundManager.class)
public class SoundManagerPlayMixin {
    @Inject(method = "play(Lnet/minecraft/client/sound/SoundInstance;)V", at = @At("HEAD"), cancellable = true)
    public void play(SoundInstance sound, CallbackInfo ci) {
        @Nullable MusicManager musicManager = Callbacks.Companion.getClientMusicManager();
        if (musicManager != null && musicManager.getMusicPack() != null && shouldIgnoreSound(sound, musicManager)) {
            ci.cancel();
        }
    }

    @Unique
    private static boolean shouldIgnoreSound(SoundInstance sound, MusicManager musicManager) {
        return sound.getId() != Constants.Companion.getAUDIO_FILE_STREAM_ID()
                && (sound.getCategory() == SoundCategory.MUSIC || ignoreAdvancement(sound, musicManager));
    }

    @Unique
    private static boolean ignoreAdvancement(SoundInstance sound, MusicManager musicManager) {
        return musicManager.hasActiveEvent(OnAdvancementGetEvent.Companion.getTypeName())
                && sound.getId() == SoundEvents.UI_TOAST_CHALLENGE_COMPLETE.getId();
    }
}
