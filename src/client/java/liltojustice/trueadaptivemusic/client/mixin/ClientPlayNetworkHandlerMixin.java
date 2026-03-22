package liltojustice.trueadaptivemusic.client.mixin;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.ScoreboardObjectiveUpdateS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
    @Inject(method = "onScoreboardObjectiveUpdate", at = @At("HEAD"), cancellable = true)
    public void onScoreboardObjectiveUpdate(ScoreboardObjectiveUpdateS2CPacket packet, CallbackInfo ci) {
        var thisObject = (ClientPlayNetworkHandler)(Object)this;
        if (thisObject.getScoreboard().getNullableObjective(packet.getName()) != null &&
                packet.getMode() == ScoreboardObjectiveUpdateS2CPacket.ADD_MODE) {
            ci.cancel();
        }
    }
}
