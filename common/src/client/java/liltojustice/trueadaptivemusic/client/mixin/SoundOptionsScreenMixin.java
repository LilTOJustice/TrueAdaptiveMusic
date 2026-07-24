package liltojustice.trueadaptivemusic.client.mixin;

import liltojustice.trueadaptivemusic.client.TrueAdaptiveMusicOptionCallback;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.OptionsSubScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.SoundOptionsScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SoundOptionsScreen.class)
public class SoundOptionsScreenMixin extends OptionsSubScreen {
    @Inject(method = "init()V", at = @At("TAIL"))
    protected void init(CallbackInfo ci) {
        SoundOptionsScreen thisObject = (SoundOptionsScreen)(Object)this;
        var trueAdaptiveMusicButton = new OptionInstance<>(
                "trueadaptivemusic",
                OptionInstance.noTooltip(),
                (optionText, value) -> optionText,
                new TrueAdaptiveMusicOptionCallback<>(thisObject.minecraft),
                "",
                option -> {}
        );

        thisObject.list.addBig(trueAdaptiveMusicButton);
    }

    public SoundOptionsScreenMixin(Screen parent, Options gameOptions, Component title) {
        super(parent, gameOptions, title);
    }
}