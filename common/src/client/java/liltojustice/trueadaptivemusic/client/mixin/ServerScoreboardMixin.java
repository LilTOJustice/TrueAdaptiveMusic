package liltojustice.trueadaptivemusic.client.mixin;

import liltojustice.trueadaptivemusic.network.TAMServerNetworking;
import liltojustice.trueadaptivemusic.network.model.ScoreboardStatePayloadType;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.world.scores.Score;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerScoreboard.class)
public class ServerScoreboardMixin {
    @Inject(method = "onScoreChanged", at = @At("HEAD"))
    public void updateScore(Score score, CallbackInfo ci) {
        var thisObject = (ServerScoreboard)(Object)this;
        var player = thisObject.server.getPlayerList().getPlayerByName(score.getOwner());
        var objective = score.getObjective();
        if (player == null || objective == null) {
            return;
        }

        TAMServerNetworking.INSTANCE.sendToClient(
                player, ScoreboardStatePayloadType.INSTANCE,
                new ScoreboardStatePayloadType.ScoreboardStatePayload(objective.getName(), score.getScore())
        );
    }
}
