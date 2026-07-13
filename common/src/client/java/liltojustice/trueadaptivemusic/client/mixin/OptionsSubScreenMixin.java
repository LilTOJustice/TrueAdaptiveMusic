package liltojustice.trueadaptivemusic.client.mixin;

import liltojustice.trueadaptivemusic.client.TrueAdaptiveMusicOptionCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.client.gui.screens.options.SoundOptionsScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OptionsSubScreen.class)
public class OptionsSubScreenMixin {
    @Inject(method = "init()V", at = @At("TAIL"))
    protected void init(CallbackInfo ci) {
        OptionsSubScreen gameOptionsScreen = (OptionsSubScreen)(Object)this;
        if (!(gameOptionsScreen instanceof SoundOptionsScreen thisObject)) {
            return;
        }

        var trueAdaptiveMusicButton = new OptionInstance<>(
                "trueadaptivemusic",
                OptionInstance.noTooltip(),
                (optionText, _) -> optionText,
                new TrueAdaptiveMusicOptionCallback<>(Minecraft.getInstance()),
                "",
                _ -> {}
        );

        if (thisObject.list != null) {
            thisObject.list.addBig(trueAdaptiveMusicButton);
        }
    }
}