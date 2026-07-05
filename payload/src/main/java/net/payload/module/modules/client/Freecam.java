

/**
 * Freecam Module
 */
package net.payload.module.modules.client;

import java.util.UUID;

import net.payload.Payload;
import net.payload.event.events.Render3DEvent;
import net.payload.event.events.TickEvent.Post;
import net.payload.event.events.TickEvent.Pre;
import net.payload.event.listeners.Render3DListener;
import net.payload.event.listeners.TickListener;
import net.payload.mixin.interfaces.ICamera;
import net.payload.module.Category;
import net.payload.module.Module;
import net.payload.settings.types.FloatSetting;
import net.payload.utils.entity.FakePlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class Freecam extends Module implements TickListener, Render3DListener {
	private FloatSetting flySpeed = FloatSetting.builder().id("freecam_speed").displayName("Speed")
			.description("Speed of the Freecam.").defaultValue(2f).minValue(0.1f).maxValue(15f).step(0.5f).build();

	private FakePlayerEntity fakePlayer;
	private Vec3d prevPos;
	private Vec3d pos;

	public Freecam() {
		super("Freecam");
		this.setCategory(Category.of("client"));
		this.setDescription("(ClientSide) Allows free movement of your camera");
		this.addSetting(flySpeed);
	}

	public void setSpeed(float speed) {
		this.flySpeed.setValue(speed);
	}

	public double getSpeed() {
		return this.flySpeed.getValue();
	}

	@Override
	public void onDisable() {
		if (fakePlayer != null)
			fakePlayer.despawn();

		Payload.getInstance().eventManager.RemoveListener(TickListener.class, this);
		Payload.getInstance().eventManager.RemoveListener(Render3DListener.class, this);
	}

	@Override
	public void onEnable() {
		Payload.getInstance().eventManager.AddListener(TickListener.class, this);
		Payload.getInstance().eventManager.AddListener(Render3DListener.class, this);

		ClientPlayerEntity player = MC.player;
		fakePlayer = new FakePlayerEntity();
		fakePlayer.copyFrom(player);
		fakePlayer.setUuid(UUID.randomUUID());
		fakePlayer.headYaw = player.headYaw;
		fakePlayer.bodyYaw = player.bodyYaw;
		fakePlayer.setPitch(player.getPitch());
		MC.world.addEntity(fakePlayer);

		Camera camera = MC.gameRenderer.getCamera();
		ICamera iCamera = (ICamera) camera;
		iCamera.setFocusedEntity(null);

		Vec3d newPos = MC.player.getPos().add(0, 1.5, 0);
		prevPos = newPos;
		pos = newPos;
		iCamera.setCameraPos(pos);
	}

	@Override
	public void onToggle() {
	}

	@Override
	public void onRender(Render3DEvent event) {
		Camera camera = MC.gameRenderer.getCamera();
		ICamera iCamera = (ICamera) camera;

		float tickDelta = event.getRenderTickCounter().getTickDelta(true);

		Vec3d interpolatedPos = new Vec3d(MathHelper.lerp(tickDelta, prevPos.x, pos.x),
				MathHelper.lerp(tickDelta, prevPos.y, pos.y), MathHelper.lerp(tickDelta, prevPos.z, pos.z));
		iCamera.setCameraPos(interpolatedPos);
	}

	public FakePlayerEntity getFakePlayer() {
		return this.fakePlayer;
	}

	@Override
	public void onTick(Pre event) {
	}

	@Override
	public void onTick(Post event) {
		if (nullCheck()) return;

		Camera camera = MC.gameRenderer.getCamera();
		Vec3d cameraPos = camera.getPos();
		prevPos = cameraPos;

		Vec3d forward = Vec3d.fromPolar(0, camera.getYaw());
		Vec3d right = Vec3d.fromPolar(0, camera.getYaw() + 90);

		Vec3d velocity = new Vec3d(0, 0, 0);

		if (MC.options.forwardKey.isPressed()) {
			velocity = velocity.add(forward.multiply(flySpeed.getValue()));
		} else if (MC.options.backKey.isPressed()) {
			velocity = velocity.subtract(forward.multiply(flySpeed.getValue()));
		}

		if (MC.options.rightKey.isPressed()) {
			velocity = velocity.add(right.multiply(flySpeed.getValue()));
		} else if (MC.options.leftKey.isPressed()) {
			velocity = velocity.subtract(right.multiply(flySpeed.getValue()));
		}

		if (MC.options.jumpKey.isPressed()) {
			velocity = velocity.add(0, flySpeed.getValue(), 0);
		} else if (MC.options.sneakKey.isPressed())
			velocity = velocity.add(0, -flySpeed.getValue(), 0);

		pos = cameraPos.add(velocity);

	}
}
