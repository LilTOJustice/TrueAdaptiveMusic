package liltojustice.trueadaptivemusic.client.mixin;

import net.minecraft.server.ServerScoreboard;
import net.minecraft.world.scores.Objective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerScoreboard.class)
public class ServerScoreboardMixin {
    @Inject(method = "stopTrackingObjective", at = @At("HEAD"), cancellable = true)
    public void stopSyncing(Objective objective, CallbackInfo ci) {
        ci.cancel();
    }
}
