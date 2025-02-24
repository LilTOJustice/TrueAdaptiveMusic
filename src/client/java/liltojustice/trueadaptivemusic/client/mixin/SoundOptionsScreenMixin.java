package liltojustice.trueadaptivemusic.client.mixin;

import liltojustice.trueadaptivemusic.client.TrueAdaptiveMusicOptionCallback;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.screen.option.SoundOptionsScreen;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SoundOptionsScreen.class)
public class SoundOptionsScreenMixin extends GameOptionsScreen {
    @Inject(method = "init()V", at = @At("TAIL"))
    protected void init(CallbackInfo ci) {
        SoundOptionsScreen thisObject = (SoundOptionsScreen)(Object)this;
        var trueAdaptiveMusicButton = new SimpleOption<>(
                "True Adaptive Music",
                SimpleOption.emptyTooltip(),
                (optionText, value) -> optionText,
                new TrueAdaptiveMusicOptionCallback<>(thisObject.client),
                "",
                option -> {}
        );

        thisObject.optionButtons.addSingleOptionEntry(trueAdaptiveMusicButton);
    }

    public SoundOptionsScreenMixin(Screen parent, GameOptions gameOptions, Text title) {
        super(parent, gameOptions, title);
    }
}