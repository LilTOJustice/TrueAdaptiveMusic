package liltojustice.trueadaptivemusic.client.mixin;

import liltojustice.trueadaptivemusic.client.TAMClient;
import liltojustice.trueadaptivemusic.client.javasucks.SoundManagerMixinHelper;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SoundManager.class)
public class SoundManagerMixin {
    @Inject(method = "play(Lnet/minecraft/client/resources/sounds/SoundInstance;)V", at = @At("HEAD"), cancellable = true)
    public void play(SoundInstance sound, CallbackInfo ci) {
        SoundManager thisObject = (SoundManager)(Object)this;

        if (sound.getSource() == SoundSource.MUSIC) {
            TAMClient.INSTANCE.setDesiredVanillaSoundEvent(SoundEvent.createVariableRangeEvent(sound.getLocation()));
        }

        if (SoundManagerMixinHelper.shouldIgnore(sound)) {
            sound.resolve(thisObject);
            ci.cancel();
        }
    }

    @Inject(method = "updateSourceVolume", at = @At("HEAD"))
    public void updateSoundVolume(SoundSource soundCategory, float f, CallbackInfo ci) {
        TAMClient.INSTANCE.refreshSoundVolume();
    }
}
