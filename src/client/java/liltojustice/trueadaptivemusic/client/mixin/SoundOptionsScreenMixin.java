package liltojustice.trueadaptivemusic.client.mixin;

import liltojustice.trueadaptivemusic.client.gui.screen.MainScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.screen.option.SoundOptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.GameOptions;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SoundOptionsScreen.class)
public class SoundOptionsScreenMixin extends GameOptionsScreen {
    @Inject(method = "init()V", at = @At("TAIL"))
    protected void init(CallbackInfo ci) {
        SoundOptionsScreen thisObject = (SoundOptionsScreen)(Object)this;
        if (thisObject.client == null) {
            return;
        }

        var trueAdaptiveMusicButton = new ButtonWidget(
                0,
                0,
                0,
                20,
                TRUE_ADAPTIVE_MUSIC_BUTTON_TEXT,
                button -> thisObject.client.setScreen(new MainScreen(parent)));
        trueAdaptiveMusicButton.setWidth(textRenderer.getWidth(TRUE_ADAPTIVE_MUSIC_BUTTON_TEXT) + 10);
        trueAdaptiveMusicButton.x = thisObject.width - trueAdaptiveMusicButton.getWidth();

        thisObject.addDrawableChild(trueAdaptiveMusicButton);
    }

    public SoundOptionsScreenMixin(Screen parent, GameOptions gameOptions, Text title) {
        super(parent, gameOptions, title);
    }

    @Unique
    private static final Text TRUE_ADAPTIVE_MUSIC_BUTTON_TEXT = Text.of("True Adaptive Music");
}