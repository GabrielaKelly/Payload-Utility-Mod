

package net.payload.mixin.interfaces;

import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.util.math.Vec3d;

@Mixin(ExplosionS2CPacket.class)
public interface IExplosionS2CPacket {
	@Mutable
	@Accessor("playerKnockback")
	void setPlayerKnockback(Optional<Vec3d> knockback);
}
