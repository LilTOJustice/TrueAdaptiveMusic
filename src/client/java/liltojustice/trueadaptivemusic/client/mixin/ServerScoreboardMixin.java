package liltojustice.trueadaptivemusic.client.mixin;

import liltojustice.trueadaptivemusic.network.model.ScoreboardStatePayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.scoreboard.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerScoreboard.class)
public class ServerScoreboardMixin {
    @Inject(method = "updateScore", at = @At("HEAD"))
    public void updateScore(ScoreboardPlayerScore score, CallbackInfo ci) {
        var thisObject = (ServerScoreboard)(Object)this;
        var player = thisObject.server.getPlayerManager().getPlayer(score.getPlayerName());
        var objective = score.getObjective();
        if (player == null || objective == null) {
            return;
        }

        ServerPlayNetworking.send(player, new ScoreboardStatePayload(objective.getName(), score.getScore()));
    }
}
