package liltojustice.trueadaptivemusic.client.mixin;

import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ServerScoreboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerScoreboard.class)
public class ServerScoreboardMixin {
    @Inject(method = "stopSyncing", at = @At("HEAD"), cancellable = true)
    public void stopSyncing(ScoreboardObjective objective, CallbackInfo ci) {
        ci.cancel();
    }
}
