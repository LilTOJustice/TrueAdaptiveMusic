package liltojustice.trueadaptivemusic.client.mixin.event;

import liltojustice.trueadaptivemusic.client.event.MusicEvent;
import liltojustice.trueadaptivemusic.client.event.types.OnJoinWorldEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class OnJoinWorldEventMixin {
    @Inject(at = @At("HEAD"), method = "setWorld(Lnet/minecraft/client/world/ClientWorld;)V")
    public void setWorld(ClientWorld world, CallbackInfo ci) {
        if (world != null) {
            MusicEvent.Companion.invokeMusicEvent(OnJoinWorldEvent.Companion.getTypeName());
        }
    }
}