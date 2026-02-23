package sidly.soundadjuster.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import sidly.soundadjuster.SoundAdjuster;

@Mixin(SoundEngine.class)
public abstract class SoundEngineMixin {
    @Shadow protected abstract float calculateVolume(float volume, SoundSource category);

    @Redirect(
            method = "play(Lnet/minecraft/client/resources/sounds/SoundInstance;)Lnet/minecraft/client/sounds/SoundEngine$PlayResult;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/sounds/SoundEngine;calculateVolume(FLnet/minecraft/sounds/SoundSource;)F",
                    ordinal = 0
            )
    )
    private float adjustVolume(SoundEngine instance, float volume, SoundSource category, @Local(name = "identifier") Identifier identifier) {
        float original = this.calculateVolume(volume, category);
        float multiplier = SoundAdjuster.getVolumeMultiplier(identifier);
        return original * multiplier;
    }
}