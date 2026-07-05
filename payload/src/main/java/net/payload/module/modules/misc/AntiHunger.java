package net.payload.module.modules.misc;

import net.payload.Payload;
import net.payload.event.events.SendMovementPacketEvent;
import net.payload.event.events.SendPacketEvent;
import net.payload.event.listeners.SendMovementPacketListener;
import net.payload.event.listeners.SendPacketListener;
import net.payload.mixin.interfaces.IPlayerMoveC2SPacket;
import net.payload.module.Category;
import net.payload.module.Module;
import net.payload.settings.types.BooleanSetting;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class AntiHunger extends Module implements SendPacketListener, SendMovementPacketListener {
	private boolean lastOnGround, ignorePacket;

	public BooleanSetting sprint = BooleanSetting.builder().id("antihunger_sprint").displayName("Sprint")
			.description("Change sprint packets.").defaultValue(true).build();

	public BooleanSetting onGround = BooleanSetting.builder().id("antihunger_onground").displayName("On Ground")
			.description("Fakes onGround.").defaultValue(true).build();

	public AntiHunger() {
		super("AntiHunger");
		this.setCategory(Category.of("Misc"));
		this.setDescription("Passively reduces the rate player hunger decreases");

		this.addSetting(sprint);
		this.addSetting(onGround);
	}

	@Override
	public void onDisable() {
		Payload.getInstance().eventManager.RemoveListener(SendPacketListener.class, this);
		Payload.getInstance().eventManager.RemoveListener(SendMovementPacketListener.class, this);
	}

	@Override
	public void onEnable() {
		Payload.getInstance().eventManager.AddListener(SendPacketListener.class, this);
		Payload.getInstance().eventManager.AddListener(SendMovementPacketListener.class, this);

		lastOnGround = MC.player.isOnGround();
	}

	@Override
	public void onToggle() {

	}

	@Override
	public void onSendPacket(SendPacketEvent event) {
		if (nullCheck()) return;

		if (ignorePacket && event.GetPacket() instanceof PlayerMoveC2SPacket) {
			ignorePacket = false;
			return;
		}

		if (MC.player != null) {
			if (MC.player.hasVehicle() || MC.player.isTouchingWater() || MC.player.isSubmergedInWater())
				return;

			if (event.GetPacket() instanceof PlayerMoveC2SPacket packet && onGround.getValue() && MC.player.isOnGround()
					&& MC.player.fallDistance <= 0.0 && !MC.interactionManager.isBreakingBlock()) {
				((IPlayerMoveC2SPacket) packet).setOnGround(false);
			}
		}

		if (event.GetPacket() instanceof ClientCommandC2SPacket packet && sprint.getValue()) {
			if (packet.getMode() == ClientCommandC2SPacket.Mode.START_SPRINTING)
				event.cancel();
		}
	}

	@Override
	public void onSendMovementPacket(SendMovementPacketEvent.Pre event) {
		if (nullCheck()) return;

		if (MC.player.isOnGround() && !lastOnGround && onGround.getValue()) {
			ignorePacket = true;
		}

		lastOnGround = MC.player.isOnGround();
	}

	@Override
	public void onSendMovementPacket(SendMovementPacketEvent.Post event) {

	}
}
