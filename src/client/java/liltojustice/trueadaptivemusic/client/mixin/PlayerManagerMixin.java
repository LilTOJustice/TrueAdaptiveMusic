package liltojustice.trueadaptivemusic.client.mixin;

import net.minecraft.network.packet.Packet;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
    @Inject(method = "sendScoreboard", at = @At("TAIL"))
    public void sendScoreboard(ServerScoreboard scoreboard, ServerPlayerEntity player, CallbackInfo ci) {
        scoreboard
                .getObjectives()
                .stream()
                .filter(objective -> scoreboard.countDisplaySlots(objective) == 0)
                .forEach(objective -> {
                    for (Packet<?> packet : scoreboard.createChangePackets(objective)) {
                        player.networkHandler.sendPacket(packet);
                    }
                });
    }
}
