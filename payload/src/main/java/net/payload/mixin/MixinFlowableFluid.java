package net.payload.mixin;

import net.minecraft.fluid.FlowableFluid;
import net.minecraft.util.math.Direction;
import net.payload.Payload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Iterator;

@Mixin(FlowableFluid.class)
public class MixinFlowableFluid {

    @Redirect(method = "getVelocity", at = @At(value = "INVOKE", target = "Ljava/util/Iterator;hasNext()Z", ordinal = 0))
    private boolean getVelocity_hasNext(Iterator<Direction> var9) {
        if (Payload.getInstance().moduleManager.antiknockback.noWaterPush.getValue() && Payload.getInstance().moduleManager.antiknockback.state.getValue()) {
            return false;
        }
        return var9.hasNext();
    }

}