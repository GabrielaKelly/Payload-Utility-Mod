package net.payload.module.modules.misc;

import java.util.UUID;

import com.mojang.authlib.GameProfile;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.payload.Payload;
import net.payload.event.events.ReceivePacketEvent;
import net.payload.event.events.TickEvent;
import net.payload.event.listeners.ReceivePacketListener;
import net.payload.event.listeners.TickListener;
import net.payload.module.Category;
import net.payload.module.Module;
import net.payload.settings.types.BooleanSetting;
import net.payload.settings.types.StringSetting;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

public class FakePlayer extends Module implements TickListener, ReceivePacketListener {
	public static OtherClientPlayerEntity fakePlayer;

	private final StringSetting playerName = StringSetting.builder().id("fakeplayer_name").displayName("Player Name")
			.description("Name of the fake player.").defaultValue("vsaw_is_the_best").build();

	private final BooleanSetting damageSimulation = BooleanSetting.builder().id("fakeplayer_damage")
			.displayName("Crystal Damage").description("Allows to player to take damage").defaultValue(true).build();

	private final BooleanSetting chatFeedback = BooleanSetting.builder().id("fakeplayer_chatresponse")
			.displayName("Chat Feedback").description("Allows to player to test autocrystal").defaultValue(true).build();

	private final BooleanSetting enableRegen = BooleanSetting.builder().id("fakeplayer_regen_enable")
			.displayName("Enable Regeneration").description("Enable regeneration effect.").defaultValue(false).build();

	private final BooleanSetting enableAbsorption = BooleanSetting.builder().id("fakeplayer_absorption_enable")
			.displayName("Enable Absorption").description("Enable absorption effect.").defaultValue(false).build();

	private final BooleanSetting enableResistance = BooleanSetting.builder().id("fakeplayer_resistance_enable")
			.displayName("Enable Resistance").description("Enable resistance effect.").defaultValue(false).build();


	public FakePlayer() {
		super("FakePlayer");

		this.setCategory(Category.of("Misc"));
		this.setDescription("Spawns a fake player entity for testing");

		this.addSetting(damageSimulation);
		this.addSetting(enableRegen);
		this.addSetting(enableAbsorption);
		this.addSetting(enableResistance);
	}

	@Override
	public void onDisable() {
		Payload.getInstance().eventManager.RemoveListener(TickListener.class, this);
		Payload.getInstance().eventManager.RemoveListener(ReceivePacketListener.class, this);

		if (fakePlayer == null)
			return;
		fakePlayer.setRemoved(Entity.RemovalReason.KILLED);
		fakePlayer.onRemoved();
		fakePlayer = null;
	}

	@Override
	public void onEnable() {
		Payload.getInstance().eventManager.AddListener(TickListener.class, this);
		Payload.getInstance().eventManager.AddListener(ReceivePacketListener.class, this);

		fakePlayer = new OtherClientPlayerEntity(MC.world,
				new GameProfile(UUID.fromString("66123666-6666-6666-6666-666666666600"), playerName.getValue())) {
			@Override
			public boolean isOnGround() {
				return true;
			}
		};

		fakePlayer.copyPositionAndRotation(MC.player);

		MC.world.addEntity(fakePlayer);

		if (enableRegen.getValue()) {
			fakePlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 9999, 2));
		}
		if (enableAbsorption.getValue()) {
			fakePlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 9999, 3));
		}
		if (enableResistance.getValue()) {
			fakePlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 9999, 1));
		}
	}

	@Override
	public void onToggle() {

	}

	@Override
	public void onReceivePacket(ReceivePacketEvent readPacketEvent) {
		if (damageSimulation.getValue() && fakePlayer != null && fakePlayer.hurtTime == 0) {
			if (fakePlayer.getOffHandStack().getItem() != Items.TOTEM_OF_UNDYING) {
				fakePlayer.setStackInHand(Hand.OFF_HAND, new ItemStack(Items.TOTEM_OF_UNDYING));
			}
			if (readPacketEvent.getPacket() instanceof ExplosionS2CPacket explosion) {
				if (MathHelper.sqrt((float) new Vec3d(explosion.center().getX(), explosion.center().getY(), explosion.center().getZ()).squaredDistanceTo(fakePlayer.getPos())) > 10) return;
				float damage;
				/*
				if (OtherBlockUtils.getBlock(new BlockPosX(explosion.center().getX(), explosion.center().getY(), explosion.center().getZ())) == Blocks.RESPAWN_ANCHOR) {
					damage = (float) AutoAnchor.INSTANCE.getAnchorDamage(new BlockPosX(explosion.getX(), explosion.getY(), explosion.getZ()), fakePlayer, fakePlayer);
				} else {
					damage = AutoCrystal.INSTANCE.calculateDamage(new Vec3d(explosion.getX(), explosion.getY(), explosion.getZ()), fakePlayer, fakePlayer);
				}

				 */
				damage = Payload.getInstance().moduleManager.crystalaura.calculateDamage(new Vec3d(explosion.center().getX(), explosion.center().getY(), explosion.center().getZ()), fakePlayer, fakePlayer);

				if (chatFeedback.getValue()) {
					sendFakePlayerMessage("Simulated Crystal Damage: " + damage);
				}

				fakePlayer.onDamaged(MC.world.getDamageSources().generic());
				if (fakePlayer.getAbsorptionAmount() >= damage) {
					fakePlayer.setAbsorptionAmount(fakePlayer.getAbsorptionAmount() - damage);
				} else {
					float damage2 = damage - fakePlayer.getAbsorptionAmount();
					fakePlayer.setAbsorptionAmount(0);
					fakePlayer.setHealth(fakePlayer.getHealth() - damage2);
				}
			}
			if (fakePlayer.isDead()) {
				fakePlayer.setHealth(10f);
				new EntityStatusS2CPacket(fakePlayer, EntityStatuses.USE_TOTEM_OF_UNDYING).apply(MC.getNetworkHandler());
			}
		}
	}

	@Override
	public void onTick(TickEvent.Pre event) {
		if (!(fakePlayer != null && !fakePlayer.isDead() && fakePlayer.clientWorld == MC.world)) {
			this.toggle();
			return;
		}
		fakePlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 9999, 2));
		if (fakePlayer.getOffHandStack().getItem() != Items.TOTEM_OF_UNDYING) {
			fakePlayer.setStackInHand(Hand.OFF_HAND, new ItemStack(Items.TOTEM_OF_UNDYING));
		}
		if (fakePlayer.isDead()) {
			fakePlayer.setHealth(10f);
			new EntityStatusS2CPacket(fakePlayer, EntityStatuses.USE_TOTEM_OF_UNDYING).apply(MC.getNetworkHandler());
		}
	}

	@Override
	public void onTick(TickEvent.Post event) {

	}

	public static void sendFakePlayerMessage(String message) {
		MinecraftClient mc = MinecraftClient.getInstance();
		if (mc.inGameHud != null) {
			mc.inGameHud.getChatHud().addMessage(Text.of(Formatting.DARK_BLUE + "[" + Formatting.BLUE + "FakePlayer"
					+ Formatting.DARK_BLUE + "] " + Formatting.RESET + message));
		}
	}
}
