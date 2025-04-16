package liltojustice.trueadaptivemusic.client.mixin;

import liltojustice.trueadaptivemusic.Constants;
import liltojustice.trueadaptivemusic.client.TAMClient;
import liltojustice.trueadaptivemusic.client.trigger.event.types.OnAdvancementGetEvent;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SoundManager.class)
public class SoundManagerPlayMixin {
    @Inject(method = "play(Lnet/minecraft/client/sound/SoundInstance;)V", at = @At("HEAD"), cancellable = true)
    public void play(SoundInstance sound, CallbackInfo ci) {
        if (TAMClient.INSTANCE.getMusicPack() != null && shouldIgnoreSound(sound)) {
            ci.cancel();
        }
    }

    @Unique
    private static boolean shouldIgnoreSound(SoundInstance sound) {
        return sound.getId() != Constants.Companion.getAUDIO_FILE_STREAM_ID()
                && (sound.getCategory() == SoundCategory.MUSIC || ignoreAdvancement(sound));
    }

    @Unique
    private static boolean ignoreAdvancement(SoundInstance sound) {
        return TAMClient.INSTANCE.hasActiveEvent(OnAdvancementGetEvent.Companion.getTypeName())
                && sound.getId() == SoundEvents.UI_TOAST_CHALLENGE_COMPLETE.getId();
    }
}
