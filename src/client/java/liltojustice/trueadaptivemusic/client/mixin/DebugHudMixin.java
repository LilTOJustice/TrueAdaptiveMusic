package liltojustice.trueadaptivemusic.client.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import liltojustice.trueadaptivemusic.client.javasucks.DebugHudMixinHelper;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(Gui.class)
public class DebugHudMixin {
    @Inject(method = "extractRenderState", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILSOFT)
    public void render(
            DeltaTracker deltaTracker,
            boolean shouldRenderLevel,
            boolean resourcesLoaded,
            CallbackInfo ci,
            @Local(name = "graphics") GuiGraphicsExtractor graphics
    ) {
        DebugHudMixinHelper.render(graphics);
    }
}
