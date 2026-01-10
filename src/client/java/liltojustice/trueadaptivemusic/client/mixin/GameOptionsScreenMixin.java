package liltojustice.trueadaptivemusic.client.mixin;

import liltojustice.trueadaptivemusic.client.TrueAdaptiveMusicOptionCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.screen.option.SoundOptionsScreen;
import net.minecraft.client.option.SimpleOption;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameOptionsScreen.class)
public class GameOptionsScreenMixin {
    @Inject(method = "init()V", at = @At("TAIL"))
    protected void init(CallbackInfo ci) {
        GameOptionsScreen gameOptionsScreen = (GameOptionsScreen)(Object)this;
        if (!(gameOptionsScreen instanceof SoundOptionsScreen thisObject)) {
            return;
        }

        var trueAdaptiveMusicButton = new SimpleOption<>(
                "True Adaptive Music",
                SimpleOption.emptyTooltip(),
                (optionText, value) -> optionText,
                new TrueAdaptiveMusicOptionCallback<>(MinecraftClient.getInstance()),
                "",
                option -> {}
        );

        if (thisObject.body != null) {
            thisObject.body.addSingleOptionEntry(trueAdaptiveMusicButton);
        }
    }
}