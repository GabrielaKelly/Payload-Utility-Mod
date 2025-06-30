

/**
 * NoSlowdown Module
 */
package net.payload.module.modules.movement;

import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.payload.Payload;
import net.payload.event.events.KeyDownEvent;
import net.payload.event.events.SendMovementPacketEvent;
import net.payload.event.events.SendPacketEvent;
import net.payload.event.events.TickEvent.Post;
import net.payload.event.events.TickEvent.Pre;
import net.payload.event.listeners.*;
import net.payload.mixin.interfaces.IEntity;
import net.payload.module.Category;
import net.payload.module.Module;
import net.payload.settings.types.BooleanSetting;
import net.payload.settings.types.EnumSetting;
import net.payload.settings.types.FloatSetting;
import net.minecraft.util.math.Vec3d;
import net.payload.utils.player.MovementUtil;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

public class NoSlowdown extends Module implements TickListener, SendPacketListener, KeyDownListener, SendMovementPacketListener {

	private enum Mode {Vanilla, Grim}

	private enum BypassMode {None, GrimSwap, Delay}

	private final EnumSetting<NoSlowdown.Mode> mode = EnumSetting.<NoSlowdown.Mode>builder()
			.id("noslow_bypass")
			.displayName("Mode")
			.defaultValue(Mode.Vanilla).build();

	private final EnumSetting<NoSlowdown.BypassMode> bypassMode = EnumSetting.<NoSlowdown.BypassMode>builder()
			.id("noslow_mode")
			.displayName("Bypass")
			.defaultValue(BypassMode.None).build();

	private final BooleanSetting soulSand = BooleanSetting.builder()
			.id("noslow_soul_sand")
			.displayName("SoulSand")
			.description("Prevents slowdown from soul sand")
			.defaultValue(true)
			.build();

	private final BooleanSetting active = BooleanSetting.builder()
			.id("noslow_gui")
			.displayName("Gui")
			.description("Prevents slowdown in GUIs")
			.defaultValue(true)
			.build();

	private final BooleanSetting sneak = BooleanSetting.builder()
			.id("noslow_sneak")
			.displayName("Sneak")
			.description("Prevents slowdown while sneaking")
			.defaultValue(false)
			.build();

	private FloatSetting slowdownMultiplier = FloatSetting.builder().id("noslow_multiplier")
			.displayName("Vanilla Multiplier").description("NoSlowdown walk speed multiplier.").defaultValue(0f).minValue(0f)
			.maxValue(1f).step(0.1f).build();

	private final Queue<ClickSlotC2SPacket> storedClicks = new LinkedList<>();
	private final AtomicBoolean pause = new AtomicBoolean();

	public NoSlowdown() {
		super("NoSlowdown");
		this.setCategory(Category.of("Movement"));
		this.setDescription("Prevents the player from being slowed down by various things");

		this.addSetting(mode);
		this.addSetting(bypassMode);
		this.addSetting(active);
		this.addSetting(sneak);
		this.addSetting(soulSand);
		this.addSetting(slowdownMultiplier);
	}

	@Override
	public void onDisable() {
		Payload.getInstance().eventManager.RemoveListener(TickListener.class, this);
		Payload.getInstance().eventManager.RemoveListener(SendPacketListener.class, this);
		Payload.getInstance().eventManager.RemoveListener(KeyDownListener.class, this);
		Payload.getInstance().eventManager.RemoveListener(SendMovementPacketListener.class, this);
	}

	@Override
	public void onEnable() {
		Payload.getInstance().eventManager.AddListener(TickListener.class, this);
		Payload.getInstance().eventManager.AddListener(SendPacketListener.class, this);
		Payload.getInstance().eventManager.AddListener(KeyDownListener.class, this);
		Payload.getInstance().eventManager.AddListener(SendMovementPacketListener.class, this);
	}

	@Override
	public void onToggle() {

	}

	@Override
	public void onTick(Pre event) {
		IEntity playerEntity = (IEntity) MC.player;

		if (mode.getValue() == Mode.Vanilla) {
			if (!playerEntity.getMovementMultiplier().equals(Vec3d.ZERO)) {
				float multiplier = slowdownMultiplier.getValue();
				if (multiplier == 0.0f) {
					playerEntity.setMovementMultiplier(Vec3d.ZERO);
				} else {
					playerEntity.setMovementMultiplier(Vec3d.ZERO.add(1, 1, 1).multiply(1 / multiplier));
				}
			}
		}
	}

	@Override
	public void onTick(Post event) {

	}

	@Override
	public void onSendMovementPacket(SendMovementPacketEvent.Pre event) {
		if (MC.player.isUsingItem() && !MC.player.isRiding() && !MC.player.isGliding()) {
			switch (mode.getValue()) {
				case Grim -> {
					if (MC.player.getActiveHand() == Hand.OFF_HAND) {
						MC.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(MC.player.getInventory().selectedSlot % 8 + 1));
						MC.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(MC.player.getInventory().selectedSlot % 7 + 2));
						MC.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(MC.player.getInventory().selectedSlot));
					} else {
						sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.OFF_HAND, id, MC.player.getYaw(), MC.player.getPitch()));
					}
				}
				case Vanilla -> {
					break;
				}
			}
		}
		if (active.getValue()) {
			if (!(MC.currentScreen instanceof ChatScreen)) {
				for (KeyBinding k : new KeyBinding[]{MC.options.backKey, MC.options.leftKey, MC.options.rightKey}) {
					k.setPressed(InputUtil.isKeyPressed(MC.getWindow().getHandle(), InputUtil.fromTranslationKey(k.getBoundKeyTranslationKey()).getCode()));
				}
				MC.options.jumpKey.setPressed(Payload.getInstance().moduleManager.elytraBounce.state.getValue() && Payload.getInstance().moduleManager.elytraBounce.autoJump.getValue() || InputUtil.isKeyPressed(MC.getWindow().getHandle(), InputUtil.fromTranslationKey(MC.options.jumpKey.getBoundKeyTranslationKey()).getCode()));
				MC.options.forwardKey.setPressed(Payload.getInstance().moduleManager.autowalk.state.getValue() || InputUtil.isKeyPressed(MC.getWindow().getHandle(), InputUtil.fromTranslationKey(MC.options.forwardKey.getBoundKeyTranslationKey()).getCode()));
				MC.options.sprintKey.setPressed(Payload.getInstance().moduleManager.sprint.state.getValue() || InputUtil.isKeyPressed(MC.getWindow().getHandle(), InputUtil.fromTranslationKey(MC.options.sprintKey.getBoundKeyTranslationKey()).getCode()));

				if (sneak.getValue()) {
					MC.options.sneakKey.setPressed(InputUtil.isKeyPressed(MC.getWindow().getHandle(), InputUtil.fromTranslationKey(MC.options.sneakKey.getBoundKeyTranslationKey()).getCode()));
				}
			}
		}
	}

	@Override
	public void onKeyDown(KeyDownEvent event) {
		if (active.getValue()) {
			if (!(MC.currentScreen instanceof ChatScreen)) {

				ElytraBounce elytraBounce = Payload.getInstance().moduleManager.elytraBounce;

				for (KeyBinding k : new KeyBinding[]{MC.options.backKey, MC.options.leftKey, MC.options.rightKey}) {
					k.setPressed(InputUtil.isKeyPressed(MC.getWindow().getHandle(), InputUtil.fromTranslationKey(k.getBoundKeyTranslationKey()).getCode()));
				}
				MC.options.jumpKey.setPressed(elytraBounce.state.getValue() && elytraBounce.autoJump.getValue() || InputUtil.isKeyPressed(MC.getWindow().getHandle(), InputUtil.fromTranslationKey(MC.options.jumpKey.getBoundKeyTranslationKey()).getCode()));
				MC.options.forwardKey.setPressed(Payload.getInstance().moduleManager.autowalk.state.getValue() || InputUtil.isKeyPressed(MC.getWindow().getHandle(), InputUtil.fromTranslationKey(MC.options.forwardKey.getBoundKeyTranslationKey()).getCode()));
				MC.options.sprintKey.setPressed(Payload.getInstance().moduleManager.sprint.state.getValue() || InputUtil.isKeyPressed(MC.getWindow().getHandle(), InputUtil.fromTranslationKey(MC.options.sprintKey.getBoundKeyTranslationKey()).getCode()));

				if (sneak.getValue()) {
					MC.options.sneakKey.setPressed(InputUtil.isKeyPressed(MC.getWindow().getHandle(), InputUtil.fromTranslationKey(MC.options.sneakKey.getBoundKeyTranslationKey()).getCode()));
				}
				/*
				MC.player.input.pressingForward = MC.options.forwardKey.isPressed();
				MC.player.input.pressingBack = MC.options.backKey.isPressed();
				MC.player.input.pressingLeft = MC.options.leftKey.isPressed();
				MC.player.input.pressingRight = MC.options.rightKey.isPressed();
				MC.player.input.movementForward = getMovementMultiplier(MC.player.input.pressingForward, MC.player.input.pressingBack);
				MC.player.input.movementSideways = getMovementMultiplier(MC.player.input.pressingLeft, MC.player.input.pressingRight);
				MC.player.input.jumping = MC.options.jumpKey.isPressed();
				MC.player.input.sneaking = MC.options.sneakKey.isPressed();

				 */
			}
		}
	}

	@Override
	public void onSendMovementPacket(SendMovementPacketEvent.Post event) {

	}

	private static float getMovementMultiplier(boolean positive, boolean negative) {
		if (positive == negative) {
			return 0.0F;
		} else {
			return positive ? 1.0F : -1.0F;
		}
	}

	@Override
	public void onSendPacket(SendPacketEvent event) {
		if (nullCheck() || !MovementUtil.isMoving() || !MC.options.jumpKey.isPressed() || pause.get())
			return;

		if (event.GetPacket() instanceof ClickSlotC2SPacket click) {
			switch (bypassMode.getValue()) {
				case GrimSwap -> {
					if (click.getActionType() != SlotActionType.PICKUP && click.getActionType() != SlotActionType.PICKUP_ALL)
						MC.getNetworkHandler().sendPacket(new CloseHandledScreenC2SPacket(0));
				}
				case Delay -> {
					storedClicks.add(click);
					event.cancel();
				}
			}
		}

		if(event.GetPacket() instanceof CloseHandledScreenC2SPacket) {
			if(bypassMode.getValue() == BypassMode.Delay) {
				pause.set(true);
				while (!storedClicks.isEmpty())
					MC.getNetworkHandler().sendPacket(storedClicks.poll());
				pause.set(false);
			}
		}
	}

	public boolean noSlow() {
		return this.state.getValue();
	}

	public boolean soulSand() {
		return this.state.getValue() && soulSand.getValue();
	}
}
