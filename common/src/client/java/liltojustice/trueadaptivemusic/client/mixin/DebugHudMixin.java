package liltojustice.trueadaptivemusic.client.mixin;

import liltojustice.trueadaptivemusic.Logger;
import liltojustice.trueadaptivemusic.client.javasucks.DebugHudMixinHelper;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class DebugHudMixin {
    @Inject(method = "render", at = @At("TAIL"))
    public void render(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        try {
            DebugHudMixinHelper.render(graphics);
        }
        catch (Exception e) {
            Logger.INSTANCE.logError(e.getMessage(), false);
        }
    }
}
