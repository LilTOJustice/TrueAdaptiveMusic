package liltojustice.trueadaptivemusic.client.mixin;

import liltojustice.trueadaptivemusic.network.model.ScoreboardStatePayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.scoreboard.ScoreHolder;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardScore;
import net.minecraft.scoreboard.ServerScoreboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerScoreboard.class)
public class ServerScoreboardMixin {
    @Inject(method = "updateScore", at = @At("HEAD"))
    public void updateScore(
            ScoreHolder scoreHolder, ScoreboardObjective objective, ScoreboardScore score, CallbackInfo ci) {
        var thisObject = (ServerScoreboard)(Object)this;
        var player = thisObject.server.getPlayerManager().getPlayer(scoreHolder.getNameForScoreboard());
        if (player == null) {
            return;
        }

        ServerPlayNetworking.send(player, new ScoreboardStatePayload(objective.getName(), score.getScore()));
    }
}
