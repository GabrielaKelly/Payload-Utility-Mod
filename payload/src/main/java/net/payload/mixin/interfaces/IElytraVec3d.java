package net.payload.mixin.interfaces;

import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Vec3d.class)
public interface IElytraVec3d {

    @Mutable
    @Accessor("x")
    void setEX(double x);

    @Mutable
    @Accessor("y")
    void setEY(double y);

    @Mutable
    @Accessor("z")
    void setEZ(double z);
}
