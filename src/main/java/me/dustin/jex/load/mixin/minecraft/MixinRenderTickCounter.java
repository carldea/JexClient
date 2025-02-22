package me.dustin.jex.load.mixin.minecraft;

import me.dustin.jex.event.misc.EventRenderTick;
import me.dustin.jex.load.impl.IRenderTickCounter;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderTickCounter.class)
public class MixinRenderTickCounter implements IRenderTickCounter {
    @Mutable
    @Shadow @Final private float tickTime;

    @Inject(method = "beginRenderTick", at = @At("HEAD"))
    public void beingRenderTick(long timeMillis, CallbackInfoReturnable<Integer> cir)
    {
        this.tickTime = ((EventRenderTick)new EventRenderTick(this.tickTime).run()).timeScale;
    }

    @Override
    public float getTimeScale() {
        return this.tickTime;
    }

    @Override
    public void setTimeScale(float timeScale) {
        this.tickTime = timeScale;
    }
}
