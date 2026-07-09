package liltojustice.trueadaptivemusic.client.mixin;

import liltojustice.trueadaptivemusic.common.network.model.ScoreboardStatePayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.ScoreHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerScoreboard.class)
public class ServerScoreboardMixin {
    @Inject(method = "onScoreChanged", at = @At("HEAD"))
    public void stopSyncing(ScoreHolder owner, Objective objective, Score score, CallbackInfo ci) {
        var thisObject = (ServerScoreboard)(Object)this;
        var player = thisObject.server.getPlayerList().getPlayerByName(owner.getScoreboardName());
        if (player == null) {
            return;
        }

        ServerPlayNetworking.send(player, new ScoreboardStatePayload(objective.getName(), score.value()));
    }
}
