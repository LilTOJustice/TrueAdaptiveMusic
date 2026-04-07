package liltojustice.trueadaptivemusic.client.mixin;

import com.mojang.blaze3d.audio.DeviceList;
import com.mojang.blaze3d.audio.Library;
import liltojustice.trueadaptivemusic.client.TAMClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.SoundManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Library.class)
public class SoundEngineMixin {
    @Inject(method = "init", at = @At("HEAD"))
    public void init(String preferredDevice, DeviceList currentDevices, boolean useHrtf, CallbackInfo ci) {
        Library thisObject = (Library)(Object)this;
        SoundManager soundManager = Minecraft.getInstance().getSoundManager();
        if (thisObject == soundManager.soundEngine.library) {
            TAMClient.INSTANCE.resetSound();
        }
    }
}
