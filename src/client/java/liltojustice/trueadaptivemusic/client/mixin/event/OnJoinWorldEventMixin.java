package liltojustice.trueadaptivemusic.client.mixin.event;

import liltojustice.trueadaptivemusic.client.trigger.event.types.OnJoinWorldEvent;
import liltojustice.trueadaptivemusicapi.TAMAPI;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class OnJoinWorldEventMixin {
    @Inject(at = @At("TAIL"), method = "handleLogin")
    public void onGameJoin(ClientboundLoginPacket packet, CallbackInfo ci) {
        TAMAPI.INSTANCE.invokeEvent(OnJoinWorldEvent.INSTANCE);
    }
}