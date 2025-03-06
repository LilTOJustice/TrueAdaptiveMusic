package liltojustice.trueadaptivemusic.client.mixin;

import liltojustice.trueadaptivemusic.client.instance.AdaptiveMusicSoundInstance;
import net.minecraft.client.sound.*;
import net.minecraft.sound.SoundCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.CompletableFuture;

@Mixin(SoundSystem.class)
public class SoundSystemMixin {
    @Unique
    protected void play(AdaptiveMusicSoundInstance instance) {
        SoundSystem thisObject = (SoundSystem)(Object)this;
        if (!thisObject.started) {
            return;
        }

        CompletableFuture<Channel.SourceManager> completableFuture = thisObject.channel
                .createSource(SoundEngine.RunMode.STREAMING);
        Channel.SourceManager sourceManager = completableFuture.join();
        if (sourceManager == null) {
            return;
        }

        thisObject.soundEndTicks.put(instance, thisObject.ticks + 20);
        thisObject.sources.put(instance, sourceManager);
        thisObject.sounds.put(SoundCategory.MUSIC, instance);

        sourceManager.run(source -> {
            source.setPitch(instance.getPitch());
            source.setVolume(instance.getVolume());
            source.disableAttenuation();
            source.setLooping(false);
            source.setRelative(false);
            source.setStream(instance.getAudioStream());
            source.play();
        });
    }

    @Inject(method = "play(Lnet/minecraft/client/sound/SoundInstance;)V", at = @At("HEAD"), cancellable = true)
    public void play(SoundInstance sound, CallbackInfo ci) {
        if (sound instanceof AdaptiveMusicSoundInstance) {
            play((AdaptiveMusicSoundInstance) sound);
            ci.cancel();
        }
    }


    public SoundSystemMixin() {
        super();
    }
}