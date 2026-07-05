package net.payload.module.modules.combat;

import net.minecraft.command.argument.EntityAnchorArgumentType.EntityAnchor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.HoglinEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.PhantomEntity;
import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ShulkerBulletEntity;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.payload.Payload;
import net.payload.event.events.LookAtEvent;
import net.payload.event.events.TickEvent;
import net.payload.event.listeners.LookAtListener;
import net.payload.event.listeners.TickListener;
import net.payload.module.Category;
import net.payload.module.Module;
import net.payload.settings.types.BooleanSetting;
import net.payload.settings.types.EnumSetting;
import net.payload.settings.types.FloatSetting;
import net.payload.utils.FindItemResult;
import net.payload.utils.entity.InventoryUtil;
import net.payload.utils.math.CacheTimer;
import net.payload.utils.player.combat.CombatUtil;
import net.payload.utils.player.combat.EntityUtil;
import net.payload.utils.rotation.AuraRotationMode;

public class KillAura extends Module implements TickListener, LookAtListener {
	private enum Priority {LowestHP, Closest}

	private enum SwapMode {
		Off, Normal, Inventory
	}

	private final FloatSetting priority = FloatSetting.builder()
			.id("killaura_prio")
			.displayName("Priority")
			.defaultValue(25f)
			.minValue(0f)
			.maxValue(100f)
			.step(5f)
			.build();
	private FloatSetting radius = FloatSetting.builder().id("killaura_radius").displayName("Radius").description("Radius that KillAura will target entities.").defaultValue(4f).minValue(0.1f).maxValue(10f).step(0.1f).build();
	private FloatSetting wallRadius = FloatSetting.builder().id("killaura_wallradius").displayName("WallRadius").description("Radius that KillAura will target entities thru walls.").defaultValue(0f).minValue(0f).maxValue(10f).step(0.5f).build();
	private BooleanSetting targetAnimals = BooleanSetting.builder().id("killaura_target_animals").displayName("Target Animals").description("Target animals.").defaultValue(false).build();
	private BooleanSetting targetMonsters = BooleanSetting.builder().id("killaura_target_monsters").displayName("Target Monsters").description("Target Monsters.").defaultValue(true).build();
	private BooleanSetting targetPlayers = BooleanSetting.builder().id("killaura_target_players").displayName("Target Players").description("Target Players.").defaultValue(true).build();
	private BooleanSetting targetFriends = BooleanSetting.builder().id("killaura_target_friends").displayName("Target Friends").description("Target Friends.").defaultValue(false).build();
	private BooleanSetting waitfordelay = BooleanSetting.builder().id("killaura_booldelay").displayName("Use Custom Delay").description("use float?").defaultValue(false).build();
	private BooleanSetting weaponOnly = BooleanSetting.builder().id("killaura_weapononly").displayName("Weapon Only").defaultValue(false).build();

	private final EnumSetting<KillAura.SwapMode> autoSwap = EnumSetting.<KillAura.SwapMode>builder()
			.id("killaura_autoswap")
			.displayName("Auto Swap")
			.description("Automatically switches to your best weapon")
			.defaultValue(KillAura.SwapMode.Normal)
			.build();

	private FloatSetting attackdelay = FloatSetting.builder().id("killaura_delay").displayName("Custom Delay").description("The max speed that KillAura will atk").defaultValue(1.0f).minValue(0.0f).maxValue(2.5f).step(0.1f).build();
	private BooleanSetting legit = BooleanSetting.builder().id("killaura_legit").displayName("Legit").description("Whether a raycast will be used to ensure that KillAura will not hit a player outside of the view").defaultValue(false).build();
	private BooleanSetting superMace = BooleanSetting.builder().id("killaura_supermace").displayName("Super Mace").description("Makes the Mace broken, use it with nofall for safety").defaultValue(false).build();

	private final EnumSetting<Priority> auraPriority = EnumSetting.<Priority>builder()
			.id("killaura_prio")
			.displayName("Priority")
			.defaultValue(Priority.Closest).build();

	private final EnumSetting<AuraRotationMode> auraRotationMode = EnumSetting.<AuraRotationMode>builder().id("killaura_rotation_mode").displayName("Rotation Mode").description("Controls how the player's view rotates.").defaultValue(AuraRotationMode.None).build();
	private Entity entityToAttack;
	private final CacheTimer auraTimer = new CacheTimer();
	private boolean prev;
	public Vec3d directionVec = null;

	public KillAura() {
		super("KillAura");
		this.setCategory(Category.of("Combat"));
		this.setDescription("Attacks enemies around you");
		this.addSetting(auraRotationMode);
		this.addSetting(auraPriority);
		this.addSetting(autoSwap);
		this.addSetting(radius);
		this.addSetting(wallRadius);
		this.addSetting(legit);
		this.addSetting(priority);
		this.addSetting(targetAnimals);
		this.addSetting(targetMonsters);
		this.addSetting(targetPlayers);
		this.addSetting(targetFriends);
		this.addSetting(waitfordelay);
		this.addSetting(attackdelay);
		this.addSetting(weaponOnly);
		this.addSetting(superMace);
	}

	@Override
	public void onDisable() {
		Payload.getInstance().eventManager.RemoveListener(TickListener.class, this);
		Payload.getInstance().eventManager.RemoveListener(LookAtListener.class, this);
	}

	@Override
	public void onEnable() {
		Payload.getInstance().eventManager.AddListener(TickListener.class, this);
		Payload.getInstance().eventManager.AddListener(LookAtListener.class, this);
		auraTimer.reset();
	}

	@Override
	public void onToggle() {
	}

	@Override
	public void onTick(TickEvent.Pre event) {
		if (nullCheck()) return;

		if (Payload.getInstance().moduleManager.autoeat.isEating()) {
			return;
		}

		if (superMace.getValue() && MC.player.getMainHandStack().getItem() == Items.MACE) {
			return;
		}

		entityToAttack = getTarget();

		//int old = MC.player.getInventory().selectedSlot;

		if (entityToAttack != null) {

			if (!EntityUtil.isHoldingWeapon(MC.player)) {
				if (autoSwap.getValue() != SwapMode.Off && !EntityUtil.isHoldingWeapon(MC.player) && entityToAttack != null) {

					int slot = -1;

					if (autoSwap.getValue() == SwapMode.Normal) {
						slot = EntityUtil.getHotbarWeaponSlot();
					}

					if (autoSwap.getValue() == SwapMode.Inventory) {
						slot = EntityUtil.getInventoryWeaponSlot();
					}

					if (slot != -1) {
						doSwap(slot);
					}

					if (autoSwap.getValue() == SwapMode.Inventory) {
						EntityUtil.syncInventory();
					}
				}
			}

			if (weaponOnly.getValue() && !EntityUtil.isHoldingWeapon(MC.player)) {
				return;
			}

			switch (auraRotationMode.getValue()) {
				case None:
					break;
				case Vanilla:
					MC.player.lookAt(EntityAnchor.EYES, entityToAttack.getEyePos());
					break;
				case Payload:
					break;
			}

			if (waitfordelay.getValue() && auraTimer.passed(attackdelay.getValue() * 1000)) {
				swing();
				auraTimer.reset();
			} else if (!waitfordelay.getValue() && MC.player.getAttackCooldownProgress(0) == 1) {
				swing();
			}
		}
	}

	public Entity getAuraEntity() {
		if (!this.state.getValue()) return null;

		return entityToAttack;
	}

	private boolean isEnemy(Entity entity) {
		// Target players based on settings
		if (entity instanceof PlayerEntity) {
			if (targetPlayers.getValue()) {
				if (targetFriends.getValue()) {
					return true;
				} else {
					return !Payload.getInstance().friendsList.contains((PlayerEntity) entity);
				}
			}
			return false;
		}

		// Target monsters (hostile mobs)
		if ((entity instanceof HostileEntity || entity instanceof PhantomEntity || entity instanceof ShulkerEntity || entity instanceof ShulkerBulletEntity || entity instanceof HoglinEntity) && targetMonsters.getValue()) {
			return true;
		}

		// Target animals (passive mobs)
		if ((entity instanceof PassiveEntity || entity instanceof HorseEntity)
				&& !(entity instanceof VillagerEntity)
				&& !(MC.player.getVehicle() instanceof HorseEntity)
				&& targetAnimals.getValue()) {
			return true;
		}

		return false;
	}

	private Entity getTarget() {
		Entity target = null;
		double distance = radius.getValue();
		double maxHealth = 36.0;
		for (Entity entity : MC.world.getEntities()) {
			if (!isEnemy(entity)) continue;
			if (!MC.player.canSee(entity) && MC.player.distanceTo(entity) > wallRadius.getValue()) {
				continue;
			}
			if (!CombatUtil.isValid(entity, radius.getValue())) continue;

			if (target == null) {
				target = entity;
				distance = MC.player.distanceTo(entity);
				maxHealth = EntityUtil.getHealth(entity);
			} else {
				if (auraPriority.getValue() == Priority.LowestHP && EntityUtil.getHealth(entity) < maxHealth) {
					target = entity;
					maxHealth = EntityUtil.getHealth(entity);
					continue;
				}
				if (auraPriority.getValue() == Priority.Closest && MC.player.distanceTo(entity) < distance) {
					target = entity;
					distance = MC.player.distanceTo(entity);
				}
			}
		}
		return target;
	}

	@Override
	public void onLook(LookAtEvent event) {
		if (Payload.getInstance().moduleManager.autoeat.isEating()) {
			return;
		}

		if (entityToAttack != null && (auraRotationMode.getValue() == AuraRotationMode.Payload)) {
			directionVec = entityToAttack.getEyePos();
			event.setTarget(directionVec, Payload.getInstance().moduleManager.rotations.steps.getValue(), priority.getValue());
		}
	}

	@Override
	public void onTick(TickEvent.Post event) {
		if (Payload.getInstance().moduleManager.autoeat.isEating()) {
			return;
		}

		if (superMace.getValue() && MC.player.getMainHandStack().getItem() == Items.MACE && MC.player.getAttackCooldownProgress(0) == 1) {
			if (entityToAttack != null) {
				int packetsRequired = Math.round((float) Math.ceil(Math.abs(200 / 10.0f)));
				for (int i = 0; i < packetsRequired; i++) {
					MC.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(false, false));
				}

				Vec3d newPos = MC.player.getPos().add(0, 200, 0);
				MC.player.networkHandler.sendPacket(
						new PlayerMoveC2SPacket.PositionAndOnGround(newPos.x, newPos.y, newPos.z, false, false));

				swing();
			} else {
				int packetsRequired = Math.round((float) Math.ceil(Math.abs(200 / 10.0f)));
				for (int i = 0; i < packetsRequired; i++) {
					MC.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(false, false));
				}

				Vec3d newPos = MC.player.getPos();
				MC.player.networkHandler.sendPacket(
						new PlayerMoveC2SPacket.PositionAndOnGround(newPos.x, newPos.y, newPos.z, false, false));
			}
		}
	}

	private void doSwap(int slot) {
		if (autoSwap.getValue() == KillAura.SwapMode.Normal) {
			InventoryUtil.switchToSlot(slot);
		} else if (autoSwap.getValue() == KillAura.SwapMode.Inventory) {
			InventoryUtil.inventorySwap(slot, MC.player.getInventory().selectedSlot);
			InventoryUtil.switchToSlot(slot);
		}
	}

	public void swing() {
		if (nullCheck()) return;
		if (state.getValue()) {
			if (legit.getValue()) {
				HitResult ray = MC.crosshairTarget;
				if (ray != null && ray.getType() == HitResult.Type.ENTITY) {
					EntityHitResult entityResult = (EntityHitResult) ray;
					Entity ent = entityResult.getEntity();
					if (ent == entityToAttack) {
						MC.interactionManager.attackEntity(MC.player, entityToAttack);
						EntityUtil.swingHand(Hand.MAIN_HAND, Payload.getInstance().moduleManager.antiCheat.swingMode.getValue());
					}
				}
			} else {
				MC.interactionManager.attackEntity(MC.player, entityToAttack);
				EntityUtil.swingHand(Hand.MAIN_HAND, Payload.getInstance().moduleManager.antiCheat.swingMode.getValue());
			}
		}
	}
}