

package net.payload.mixin.interfaces;

import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = EntityVelocityUpdateS2CPacket.class)
public interface IEntityVelocityUpdateS2CPacket {
    @Mutable
    @Accessor("velocityX")
    void setVelocityX(int velX);

    @Mutable
    @Accessor("velocityY")
    void setVelocityY(int velY);

    @Mutable
    @Accessor("velocityZ")
    void setVelocityZ(int velZ);

    @Accessor("entityId")
    int getId();
}