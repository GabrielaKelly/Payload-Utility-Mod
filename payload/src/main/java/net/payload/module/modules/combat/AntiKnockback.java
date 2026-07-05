

/**
 * AntiKnockback Module
 */
package net.payload.module.modules.combat;

import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.util.math.Direction;
import net.payload.Payload;
import net.payload.event.events.EntityVelocityUpdateEvent;
import net.payload.event.events.EntityVelocityUpdateListener;
import net.payload.event.events.ReceivePacketEvent;
import net.payload.event.events.TickEvent;
import net.payload.event.listeners.ReceivePacketListener;
import net.payload.event.listeners.TickListener;
import net.payload.mixin.interfaces.IEntityVelocityUpdateS2CPacket;
import net.payload.module.Category;
import net.payload.module.Module;
import net.payload.settings.types.BooleanSetting;
import net.payload.settings.types.EnumSetting;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;

public class AntiKnockback extends Module implements ReceivePacketListener, TickListener, EntityVelocityUpdateListener {

	public enum Mode {
		Vanilla, Grim, WallOnly
	}

	private final EnumSetting<AntiKnockback.Mode> mode = EnumSetting.<AntiKnockback.Mode>builder()
			.id("antiknockback_mode")
			.displayName("Mode")
			.description("Velocity mode")
			.defaultValue(AntiKnockback.Mode.Vanilla)
			.build();

	private BooleanSetting entityknockback = BooleanSetting.builder().id("antiknockback_entityknockback").displayName("No Entity Knockback")
			.defaultValue(true).build();

	public BooleanSetting noPush = BooleanSetting.builder().id("antiknockback_nopush").displayName("No Push")
			.description("Prevents being pushed by entites.").defaultValue(true).build();

	public BooleanSetting noWaterPush = BooleanSetting.builder().id("antiknockback_nowaterpush").displayName("No Water Push")
			.description("Prevents being pushed by water.").defaultValue(true).build();
	
	private BooleanSetting pauseInLiquid = BooleanSetting.builder().id("antiknockback_noliquid").displayName("Liquid Pause")
			.defaultValue(false).build();

	private BooleanSetting noFishBob = BooleanSetting.builder().id("antiknockback_nofishbob").displayName("No Fishing")
			.defaultValue(true).build();

	private BooleanSetting noExplosions = BooleanSetting.builder().id("antiknockback_noexplosions").displayName("No Explosions")
			.defaultValue(true).build();

	public AntiKnockback() {
		super("AntiKnockback");

		this.setCategory(Category.of("Combat"));
		this.setDescription("Prevents knockback");

		this.addSetting(mode);
		this.addSetting(entityknockback);
		this.addSetting(noPush);
		this.addSetting(noWaterPush);
		this.addSetting(pauseInLiquid);
		this.addSetting(noFishBob);
		this.addSetting(noExplosions);
	}

	public boolean cancelVelocity = false;

	public boolean getNoPush() {
		return this.noPush.getValue();
	}

	public boolean getGrimVelocity() {
        return mode.getValue() == Mode.Grim;
	}

	@Override
	public void onDisable() {
		Payload.getInstance().eventManager.RemoveListener(EntityVelocityUpdateListener.class, this);
		Payload.getInstance().eventManager.RemoveListener(ReceivePacketListener.class, this);
		Payload.getInstance().eventManager.RemoveListener(TickListener.class, this);

		if (cancelVelocity && mode.getValue() == Mode.Grim) {
			float yaw = Payload.getInstance().rotationManager.rotationYaw;
			float pitch = Payload.getInstance().rotationManager.rotationPitch;
			/*
			float yaw = Managers.ROTATION.getServerYaw();
			float pitch = Managers.ROTATION.getServerPitch();

			 */


			MC.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.Full(
					MC.player.getX(),
					MC.player.getY(),
					MC.player.getZ(),
					yaw,
					pitch,
					MC.player.isOnGround()	,
					MC.player.horizontalCollision
			));

			cancelVelocity = false;
		}
	}

	@Override
	public void onEnable() {
		Payload.getInstance().eventManager.AddListener(EntityVelocityUpdateListener.class, this);
		Payload.getInstance().eventManager.AddListener(ReceivePacketListener.class, this);
		Payload.getInstance().eventManager.AddListener(TickListener.class, this);
	}

	@Override
	public void onToggle() {

	}

	@Override
	public void onTick(TickEvent.Pre event) {
		if (this.cancelVelocity) {
			if ((this.mode.getValue() == Mode.Grim || this.mode.getValue() == Mode.WallOnly) && Payload.getInstance().antiCheatManager.hasPassed(100L)) {
				/*
				float yaw = Managers.ROTATION.getServerYaw();
				float pitch = Managers.ROTATION.getServerPitch();


				 */

				float yaw = Payload.getInstance().rotationManager.rotationYaw;
				float pitch = Payload.getInstance().rotationManager.rotationPitch;
				// Send position and rotation packet
				MC.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.Full(
						MC.player.getX(),
						MC.player.getY(),
						MC.player.getZ(),
						yaw,
						pitch,
						MC.player.isOnGround(),
						MC.player.horizontalCollision
				));

				MC.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK,
						MC.player.isCrawling() ? MC.player.getBlockPos() : MC.player.getBlockPos().up(), Direction.DOWN));
				/*

				// Send sneak state packet
				Managers.NETWORK.sendPacket(new PlayerActionPacket(
						PlayerAction.STOP_SNEAKING,
						player.isSneaking() ? player.getBlockPos() : player.getBlockPos().down(),
						Direction.UP
				));

				 */
			}

			this.cancelVelocity = false;
		}

		/*
		if (MC.player != null && (MC.player.isTouchingWater() || MC.player.isSubmergedInWater() || MC.player.isInLava()))
			return;

		if (flag) {
			if (lagBackTimer.passed(100)) {
				Payload.getInstance().rotationManager.snapBack();
				MC.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK,
						MC.player.isCrawling() ? MC.player.getBlockPos() : MC.player.getBlockPos().up(), Direction.DOWN));
				//MC.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, BlockPos.ofFloored(MC.player.getPos()), MC.player.getHorizontalFacing().getOpposite()));
			}
			flag = false;
		}

		 */
	}

	@Override
	public void onTick(TickEvent.Post event) {

	}

	@Override
	public void onUpdateVel(EntityVelocityUpdateEvent event) {
		if ((MC.player.isTouchingWater() || MC.player.isSubmergedInWater() || MC.player.isInLava()) && pauseInLiquid.getValue())
			return;

		if (mode.getValue() == Mode.WallOnly) {
			if (!Payload.getInstance().antiCheatManager.hasPassed(100)) {
				return;
			}

			boolean insideBlock = Payload.getInstance().playerManager.insideBlock;
			if (!insideBlock) return;
			event.cancel();
			cancelVelocity = true;
		}
	}

	@Override
	public void onReceivePacket(ReceivePacketEvent event) {
		if (nullCheck()) return;

		if ((MC.player.isTouchingWater() || MC.player.isSubmergedInWater() || MC.player.isInLava()) && pauseInLiquid.getValue()) {
			return;
		}

		if (noFishBob.getValue()) {
			if (event.getPacket() instanceof EntityStatusS2CPacket packet && packet.getStatus() == 31 && packet.getEntity(MC.world) instanceof FishingBobberEntity fishHook) {
				if (fishHook.getHookedEntity() == MC.player) {
					event.cancel();
				}
			}
		}

		Packet<?> packet = event.getPacket();

		if (packet instanceof ExplosionS2CPacket) {
			if (noExplosions.getValue()) {
				switch (mode.getValue()) {
					case Vanilla:
						event.cancel();
						break;

					case Grim:
						if (!Payload.getInstance().antiCheatManager.hasPassed(100L)) {
							return;
						}
						event.cancel();
						cancelVelocity = true;
						break;
				}
			}
		}

		if (packet instanceof EntityVelocityUpdateS2CPacket velocityPacket) {
            if (entityknockback.getValue()) {
				if (velocityPacket.getEntityId() != MC.player.getId()) {
					return;
				}

				switch (mode.getValue()) {
					case Vanilla:
						((IEntityVelocityUpdateS2CPacket) packet).setVelocityX(0);
						((IEntityVelocityUpdateS2CPacket) packet).setVelocityY(0);
						((IEntityVelocityUpdateS2CPacket) packet).setVelocityZ(0);
						break;

					case Grim:
						if (!Payload.getInstance().antiCheatManager.hasPassed(100L)) {
							return;
						}
						event.cancel();
						cancelVelocity = true;
				}
			}
		}
	}
	/*

	@Override
	public void onReceivePacket(ReceivePacketEvent event) {
		if (nullCheck()) return;
		if ((MC.player.isTouchingWater() || MC.player.isSubmergedInWater() || MC.player.isInLava()) && pauseInLiquid.getValue())
			return;

		if (noFishBob.getValue()) {
			if (event.getPacket() instanceof EntityStatusS2CPacket packet && packet.getStatus() == 31 && packet.getEntity(MC.world) instanceof FishingBobberEntity fishHook) {
				if (fishHook.getHookedEntity() == MC.player) {
					event.cancel();
				}
			}
		}

		if (event.GetPacket() instanceof PlayerPositionLookS2CPacket) {
			lagBackTimer.reset();
		}

		if (mode.getValue() == Mode.Grim || mode.getValue() == Mode.WallOnly) {
			if (!lagBackTimer.passed(100)) {
				return;
			}
			boolean insideBlock = Payload.getInstance().playerManager.insideBlock;
			if (mode.getValue() == Mode.WallOnly && !insideBlock) return;

			if (noExplosions.getValue()) {
				if (event.getPacket() instanceof ExplosionS2CPacket explosion) {
					IExplosionS2CPacket esp = (IExplosionS2CPacket) (Object) explosion;
					Vec3d v3d = new Vec3d(0, 0, 0);
					esp.setPlayerKnockback(Optional.ofNullable(v3d));
					flag = true;
				}
			}
		} else {
			float h = horizontal.getValue() / 100;
			float v = vertical.getValue() / 100;
			if (event.getPacket() instanceof ExplosionS2CPacket explosion) {
				IExplosionS2CPacket esp = (IExplosionS2CPacket) (Object) explosion;
				Vec3d v3d = new Vec3d(0, 0, 0);
				esp.setPlayerKnockback(Optional.ofNullable(v3d));

				if (noExplosions.getValue()) event.cancel();
				return;
			}

			if (event.getPacket() instanceof EntityVelocityUpdateS2CPacket packet) {
				if (packet.getEntityId() == MC.player.getId()) {
					if (horizontal.getValue() == 0 && vertical.getValue() == 0) {
						event.cancel();
					} else {
						((IEntityVelocityUpdateS2CPacket) packet).setVelocityX((int) (packet.getVelocityX() * h));
						((IEntityVelocityUpdateS2CPacket) packet).setVelocityY((int) (packet.getVelocityY() * v));
						((IEntityVelocityUpdateS2CPacket) packet).setVelocityZ((int) (packet.getVelocityZ() * h));
					}
				}
			}
		}

	}

	 */
}