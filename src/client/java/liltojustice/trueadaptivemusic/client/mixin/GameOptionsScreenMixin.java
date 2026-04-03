package liltojustice.trueadaptivemusic.client.mixin;

import com.mojang.datafixers.util.Unit;
import liltojustice.trueadaptivemusic.client.TrueAdaptiveMusicOptionCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.screens.options.SoundOptionsScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SoundOptionsScreen.class)
public class GameOptionsScreenMixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    protected void init(CallbackInfo ci) {
        SoundOptionsScreen soundOptionsScreen = (SoundOptionsScreen) (Object)this;

        var trueAdaptiveMusicButton = new OptionInstance<>(
                "trueadaptivemusic",
                OptionInstance.noTooltip(),
                (optionText, _) -> optionText,
                new TrueAdaptiveMusicOptionCallback(Minecraft.getInstance()),
                Unit.INSTANCE,
                _ -> {}
        );

        if (soundOptionsScreen.list != null) {
            soundOptionsScreen.list.addSmall(trueAdaptiveMusicButton);
        }
    }
}