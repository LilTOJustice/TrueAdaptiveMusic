package liltojustice.trueadaptivemusic.client.mixin;

import com.mojang.blaze3d.audio.DeviceList;
import com.mojang.blaze3d.audio.Library;
import liltojustice.trueadaptivemusic.client.TAMClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Library.class)
public class SoundEngineMixin {
    @Inject(method = "init", at = @At("TAIL"))
    public void init(String preferredDevice, DeviceList currentDevices, boolean useHrtf, CallbackInfo ci) {
        TAMClient.INSTANCE.initialize();
    }
}
