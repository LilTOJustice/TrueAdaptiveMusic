package liltojustice.trueadaptivemusic.client.mixin;

import liltojustice.trueadaptivemusic.client.TAMClient;
import liltojustice.trueadaptivemusic.client.trigger.event.types.OnAdvancementGetEvent;
import net.minecraft.client.sound.*;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(SoundManager.class)
public class SoundManagerMixin {
    @Inject(method = "play(Lnet/minecraft/client/sound/SoundInstance;)Lnet/minecraft/client/sound/SoundSystem$PlayResult;", at = @At("HEAD"), cancellable = true)
    public void play(SoundInstance sound, CallbackInfoReturnable<SoundSystem.PlayResult> cir) {
        if (shouldIgnore(sound)) {
            cir.setReturnValue(SoundSystem.PlayResult.STARTED);
        }
    }

    @Inject(method = "pauseAllExcept", at = @At("HEAD"), cancellable = true)
    public void pauseAllExcept(CallbackInfo ci) {
        SoundManager thisObject = (SoundManager)(Object)this;
        thisObject.soundSystem.sources.keySet().forEach(instance ->
        {
            if (!TAMClient.INSTANCE.hasSoundInstance(instance)) {
                Channel.SourceManager source = thisObject.soundSystem.sources.get(instance);
                if (source != null) {
                    source.run(Source::pause);
                }
            }
        });
        ci.cancel();
    }

    @Inject(method = "stopAll", at = @At("HEAD"))
    public void stopAll(CallbackInfo ci) {
        TAMClient.INSTANCE.resetCache();
    }

    @Unique
    private boolean shouldIgnore(SoundInstance sound) {
        return TAMClient.INSTANCE.getMusicPack() != null
                && !TAMClient.INSTANCE.hasSoundInstance(sound)
                && (sound.getCategory() == SoundCategory.MUSIC || uiToastCheck(sound));
    }

    @Unique
    private boolean uiToastCheck(SoundInstance sound) {
        var events = Objects.requireNonNull(TAMClient.INSTANCE.getCurrentPredicateResult()).getEvents();
        var soundId = sound.getId().toString();

        return soundId.equals(CHALLENGE_COMPLETE)
                && events.stream().anyMatch(event -> event instanceof OnAdvancementGetEvent);
    }

    @Unique
    private static final String CHALLENGE_COMPLETE = SoundEvents.UI_TOAST_CHALLENGE_COMPLETE.id().toString();
}
