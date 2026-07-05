

/**
 * Aimbot Module
 */
package net.payload.module.modules.combat;

import net.payload.Payload;
import net.payload.event.events.LookAtEvent;
import net.payload.event.events.Render3DEvent;
import net.payload.event.events.TickEvent;
import net.payload.event.listeners.LookAtListener;
import net.payload.event.listeners.Render3DListener;
import net.payload.event.listeners.TickListener;
import net.payload.module.Category;
import net.payload.module.Module;
import net.payload.settings.types.BooleanSetting;
import net.payload.settings.types.EnumSetting;
import net.payload.settings.types.FloatSetting;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.payload.utils.render.Render3D;

public class Aimbot extends Module implements TickListener, Render3DListener, LookAtListener {

	private LivingEntity temp = null;

	public enum RotMode {
		Vanilla, Payload
	}

	private final EnumSetting<Aimbot.RotMode> rotman = EnumSetting.<Aimbot.RotMode>builder()
			.id("aimbot_rotmode")
			.displayName("Rotate Mode")
			.defaultValue(RotMode.Payload)
			.build();

	private BooleanSetting targetAnimals = BooleanSetting.builder().id("aimbot_target_mobs").displayName("Target Mobs")
			.description("Target mobs.").defaultValue(false).build();

	private BooleanSetting targetPlayers = BooleanSetting.builder().id("aimbot_target_players")
			.displayName("Target Players").description("Target Players.").defaultValue(true).build();

	private BooleanSetting targetFriends = BooleanSetting.builder().id("aimbot_target_friends")
			.displayName("Target Friends").description("Target Friends.").defaultValue(true).build();

	private FloatSetting frequency = FloatSetting.builder().id("aimbot_frequency").displayName("Ticks")
			.description("How frequent the aimbot updates (Lower = Laggier)").defaultValue(1.0f).minValue(1.0f)
			.maxValue(1.0f).step(1.0f).build();

	private FloatSetting radius = FloatSetting.builder().id("aimbot_radius").displayName("Radius")
			.description("Radius that the aimbot will lock onto a target.").defaultValue(64.0f).minValue(1.0f)
			.maxValue(256.0f).step(1.0f).build();

	private FloatSetting rotationSpeed = FloatSetting.builder().id("aimbot_rotation_speed")
			.displayName("Rotation Speed").description("Speed of the rotation.").defaultValue(1.0f).minValue(0.1f)
			.maxValue(5.0f).step(0.1f).build();

	private final FloatSetting priority = FloatSetting.builder()
			.id("aimbot_prio")         // Consistent ID naming convention
			.displayName("Priority")         // User-friendly display name
			.defaultValue(5f)                       // Default value as float
			.minValue(0f)                          // Minimum value constraint
			.maxValue(100f)                         // Added reasonable max value
			.step(5f)                              // Integer-like stepping
			.build();


	private int currentTick = 0;

	public Aimbot() {
		super("Aimbot");
		this.setCategory(Category.of("Combat"));
		this.setDescription("Locks your crosshair towards a desired entity");

		this.addSetting(rotman);
		this.addSetting(priority);
		this.addSetting(rotationSpeed);
		this.addSetting(targetAnimals);
		this.addSetting(targetPlayers);
		this.addSetting(targetFriends);
		this.addSetting(frequency);
		this.addSetting(radius);
	}

	@Override
	public void onDisable() {
		Payload.getInstance().eventManager.RemoveListener(TickListener.class, this);
		Payload.getInstance().eventManager.RemoveListener(Render3DListener.class, this);
		Payload.getInstance().eventManager.RemoveListener(LookAtListener.class, this);
	}
		@Override
		public void onEnable () {
			Payload.getInstance().eventManager.AddListener(TickListener.class, this);
			Payload.getInstance().eventManager.AddListener(Render3DListener.class, this);
			Payload.getInstance().eventManager.AddListener(LookAtListener.class, this);
		}

		@Override
		public void onToggle () {

		}

		@Override
		public void onRender (Render3DEvent event){
			if (temp != null && rotman.getValue() == RotMode.Vanilla) {
				Vec3d offset = Render3D.getEntityPositionOffsetInterpolated(temp,
						event.getRenderTickCounter().getTickDelta(true));
				Vec3d targetPos = temp.getEyePos().add(offset);
				Vec3d playerPos = MC.player.getEyePos();
				Vec3d direction = targetPos.subtract(playerPos).normalize();

				float yaw = (float) Math.toDegrees(Math.atan2(direction.z, direction.x)) - 90F;
				float pitch = (float) -Math.toDegrees(
						Math.atan2(direction.y, Math.sqrt(direction.x * direction.x + direction.z * direction.z)));

				float currentYaw = MC.player.getYaw();
				float currentPitch = MC.player.getPitch();

				float deltaYaw = MathHelper.wrapDegrees(yaw - currentYaw);
				float deltaPitch = MathHelper.wrapDegrees(pitch - currentPitch);

				float speed = rotationSpeed.getValue();
				float smoothYaw = currentYaw + MathHelper.clamp(deltaYaw, -speed, speed);
				float smoothPitch = currentPitch + MathHelper.clamp(deltaPitch, -speed, speed);

				if (Math.abs(deltaYaw) > 180) {
					smoothYaw = currentYaw - MathHelper.clamp(deltaYaw, -speed, speed);
				}

				MC.player.setYaw(smoothYaw);
				MC.player.setPitch(smoothPitch);
			}
		}

		@Override
		public void onTick (TickEvent.Pre event){

		}

		@Override
		public void onTick (TickEvent.Post event){
			currentTick++;

			float radiusSqr = radius.getValue() * radius.getValue();

			if (currentTick >= frequency.getValue()) {
				LivingEntity entityFound = null;

				// Check for players within range of the player.
				if (this.targetPlayers.getValue()) {
					for (AbstractClientPlayerEntity entity : MC.world.getPlayers()) {
						// Skip player if targetFriends is false and the FriendsList contains the
						// entity.
						if (entity == MC.player)
							continue;

						if (!targetFriends.getValue() && Payload.getInstance().friendsList.contains(entity))
							continue;

						if (entityFound == null)
							entityFound = (LivingEntity) entity;
						else {
							double entityDistanceToPlayer = entity.squaredDistanceTo(MC.player);
							if (entityDistanceToPlayer < entityFound.squaredDistanceTo(MC.player)
									&& entityDistanceToPlayer < radiusSqr) {
								entityFound = entity;
							}
						}
					}
				}

				if (this.targetAnimals.getValue()) {
					for (Entity entity : MC.world.getEntities()) {
						if (entity instanceof LivingEntity) {
							if (entity instanceof ClientPlayerEntity)
								continue;

							double entityDistanceToPlayer = entity.squaredDistanceTo(MC.player);
							if (entityDistanceToPlayer >= radiusSqr)
								continue;

							if (entityFound == null)
								entityFound = (LivingEntity) entity;
							else if (entityDistanceToPlayer < entityFound.squaredDistanceTo(MC.player)) {
								entityFound = (LivingEntity) entity;
							}
						}
					}
				}

				temp = entityFound;
				currentTick = 0;
			} else {
				if (temp != null && temp.squaredDistanceTo(MC.player) >= radiusSqr) {
					temp = null;
				}
			}
		}

		@Override
		public void onLook (LookAtEvent event){
			if (temp != null && rotman.getValue() == RotMode.Payload) {
				event.setTarget(temp.getEyePos(), Payload.getInstance().moduleManager.rotations.steps.getValue(), priority.getValue());
			}

		}
	}