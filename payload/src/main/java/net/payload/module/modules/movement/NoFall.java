package net.payload.module.modules.movement;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.payload.module.Category;
import net.payload.settings.types.BooleanSetting;
import net.payload.settings.types.EnumSetting;
import net.payload.settings.types.FloatSetting;

import net.payload.Payload;
import net.payload.event.events.TickEvent.Post;
import net.payload.event.events.TickEvent.Pre;
import net.payload.event.listeners.TickListener;
import net.payload.module.Module;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.OnGroundOnly;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class NoFall extends Module implements TickListener {
	private static final Set<Packet<?>> PACKET_CACHE = new HashSet<>();

	public enum Mode {
		Packet, Spoof, Grim
	}

	private final EnumSetting<NoFall.Mode> mode = EnumSetting.<NoFall.Mode>builder()
			.id("nofall_mode")
			.displayName("Mode")
			.description("")
			.defaultValue(NoFall.Mode.Packet)
			.build();

	public BooleanSetting alwaysOn = BooleanSetting.builder()
			.id("nofall_alwayson")
			.displayName("Always Triggered")
			.defaultValue(false)
			.build();

	private FloatSetting fallDistance = FloatSetting.builder()
			.id("nofall_falldistance")
			.displayName("Trigger Velocity")
			.defaultValue(0.5f)
			.minValue(0f)
			.maxValue(2f)
			.step(0.1f)
			.build();

	private BooleanSetting allowElytras = BooleanSetting.builder()
			.id("nofall_allowelytras")
			.displayName("Allow Elytras")
			.defaultValue(false)
			.build();


	public NoFall() {
		super("NoFall");

		this.setName("No-Fall");
		this.setCategory(Category.of("Movement"));
		this.setDescription("Prevents fall damage.");

		this.addSetting(mode);
		this.addSetting(alwaysOn);
		this.addSetting(fallDistance);
		this.addSetting(allowElytras);

	}

	@Override
	public void onDisable() {
		Payload.getInstance().eventManager.RemoveListener(TickListener.class, this);
	}

	@Override
	public void onEnable() {
		Payload.getInstance().eventManager.AddListener(TickListener.class, this);
	}

	@Override
	public void onToggle() {

	}

	@Override
	public void onTick(Pre event) {
		if (nullCheck()) return;

		ClientPlayerEntity player = MC.player;

		if (player.isCreative())
			return;

		if (!allowElytras.getValue() && MC.player.isGliding())
			return;

		if ((Objects.requireNonNull(mode.getValue()) == Mode.Packet && isDamage(player)) || (Objects.requireNonNull(mode.getValue()) == Mode.Packet && alwaysOn.getValue())) {
			MC.player.networkHandler.sendPacket(new OnGroundOnly(true, false));
		}
		else if ((Objects.requireNonNull(mode.getValue()) == Mode.Grim && isDamage(player)) || (Objects.requireNonNull(mode.getValue()) == Mode.Grim && alwaysOn.getValue())) {
			MC.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(MC.player.getX(), MC.player.getY() + 1.0E-9, MC.player.getZ(), MC.player.getYaw(), MC.player.getPitch(), true, false));
			MC.player.stopGliding();
		}
	}

	@Override
	public void onTick(Post event) {

	}

	public double getFallDist() {
		return fallDistance.getValue();
	}

	public void sendPacket(Packet<?> packet) {
		if (MC.getNetworkHandler() != null) {
			PACKET_CACHE.add(packet);
			MC.getNetworkHandler().sendPacket(packet);
		}
	}

	private boolean isDamage(ClientPlayerEntity player)
	{
		return player.getVelocity().y < -fallDistance.getValue();
	}

	public boolean getFallingSpoofMode() {
        return mode.getValue() == Mode.Spoof;
	}


	}
