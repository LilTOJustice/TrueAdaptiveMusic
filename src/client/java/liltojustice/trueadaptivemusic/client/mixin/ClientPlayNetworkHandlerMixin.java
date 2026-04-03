package liltojustice.trueadaptivemusic.client.mixin;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundSetScorePacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPlayNetworkHandlerMixin {
    @Inject(method = "handleSetScore", at = @At("HEAD"), cancellable = true)
    public void onScoreboardObjectiveUpdate(ClientboundSetScorePacket packet, CallbackInfo ci) {
        var thisObject = (ClientPacketListener)(Object)this;
        if (thisObject.scoreboard().getObjective(packet.objectiveName()) != null) {
            ci.cancel();
        }
    }
}
