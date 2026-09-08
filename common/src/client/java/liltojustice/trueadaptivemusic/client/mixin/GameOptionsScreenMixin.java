package liltojustice.trueadaptivemusic.client.mixin;

import com.mojang.datafixers.util.Unit;
import liltojustice.trueadaptivemusic.Constants;
import liltojustice.trueadaptivemusic.client.TAMClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.screens.options.SoundOptionsScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import liltojustice.trueadaptivemusic.client.TrueAdaptiveMusicOptionCallback;

@Mixin(SoundOptionsScreen.class)
public class GameOptionsScreenMixin {
    @Inject(method = "addOptions", at = @At("TAIL"))
    protected void addOptions(CallbackInfo ci) {
        if (TAMClient.INSTANCE.getOptions().getDisableTrueAdaptiveMusicButton()) {
            return;
        }

        SoundOptionsScreen soundOptionsScreen = (SoundOptionsScreen) (Object)this;

        var trueAdaptiveMusicButton = new OptionInstance<>(
                Constants.TRUE_ADAPTIVE_MUSIC,
                OptionInstance.noTooltip(),
                (optionText, _) -> optionText,
                new TrueAdaptiveMusicOptionCallback(Minecraft.getInstance()),
                Unit.INSTANCE,
                _ -> {}
        );

        if (soundOptionsScreen.list != null) {
            soundOptionsScreen.list.addBig(trueAdaptiveMusicButton);
        }
    }
}