package liltojustice.trueadaptivemusic.client.mixin;

import liltojustice.trueadaptivemusic.client.TAMClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.SoundEngine;
import net.minecraft.client.sound.SoundManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SoundEngine.class)
public class SoundEngineMixin {
    @Inject(method = "init", at = @At("HEAD"))
    public void init(String deviceSpecifier, boolean directionalAudio, CallbackInfo ci) {
        SoundEngine thisObject = (SoundEngine)(Object)this;
        SoundManager soundManager = MinecraftClient.getInstance().getSoundManager();
        if (thisObject == soundManager.soundSystem.soundEngine) {
            TAMClient.INSTANCE.resetSound();
        }
    }
}
