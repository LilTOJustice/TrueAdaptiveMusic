package liltojustice.trueadaptivemusic.client.mixin;

import net.minecraft.network.protocol.Packet;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Server.class)
public class PlayerManagerMixin {
    @Inject(method = "sendScoreboard", at = @At("TAIL"))
    public void sendScoreboard(ServerScoreboard scoreboard, ServerPlayer player, CallbackInfo ci) {
        scoreboard
                .getObjectives()
                .stream()
                .filter(objective -> scoreboard.getObjectiveDisplaySlotCount(objective) == 0)
                .forEach(objective -> {
                    for (Packet<?> packet : scoreboard.getStartTrackingPackets(objective)) {
                        player.connection.send(packet);
                    }
                });
    }
}
