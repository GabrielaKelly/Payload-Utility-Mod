

package net.payload.module.modules.movement;

import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundEvents;
import net.payload.Payload;
import net.payload.event.events.MouseClickEvent;
import net.payload.event.listeners.MouseClickListener;
import net.payload.module.Category;
import net.payload.module.Module;
import net.payload.settings.types.FloatSetting;
import net.payload.utils.types.MouseAction;
import net.payload.utils.types.MouseButton;
import net.minecraft.client.render.Camera;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public class ClickTP extends Module implements MouseClickListener {

	private FloatSetting distance = FloatSetting.builder().id("clicktp_distance").displayName("Max Distance")
			.description("Max Distance to teleport.").defaultValue(10f).minValue(1.0f).maxValue(200f).step(1.0f)
			.build();

	public ClickTP() {
		super("ClickTP");
		this.setCategory(Category.of("Movement"));
		this.setDescription("Lets you teleport where you click, very funny in smps");

		this.addSetting(distance);
	}

	@Override
	public void onDisable() {
		Payload.getInstance().eventManager.RemoveListener(MouseClickListener.class, this);
	}

	@Override
	public void onEnable() {
		Payload.getInstance().eventManager.AddListener(MouseClickListener.class, this);
	}

	@Override
	public void onToggle() {

	}

	@Override
	public void onMouseClick(MouseClickEvent event) {

		if (event.button == MouseButton.RIGHT && event.action == MouseAction.DOWN) {
			Camera camera = MC.gameRenderer.getCamera();

			if (camera != null) {
				Vec3d direction = Vec3d.fromPolar(camera.getPitch(), camera.getYaw()).multiply(210);
				Vec3d targetPos = camera.getPos().add(direction);

				RaycastContext context = new RaycastContext(camera.getPos(), targetPos,
						RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, MC.player);

				HitResult raycast = MC.world.raycast(context);

				if (raycast.getType() == HitResult.Type.BLOCK) {
					BlockHitResult raycastBlock = (BlockHitResult) raycast;
					BlockPos pos = raycastBlock.getBlockPos();
					Direction side = raycastBlock.getSide();

					Vec3d newPos = new Vec3d(pos.getX() + 0.5 + side.getOffsetX(), pos.getY() + 1,
							pos.getZ() + 0.5 + side.getOffsetZ());
					int packetsRequired = (int) Math.ceil(MC.player.getPos().distanceTo(newPos) / 10) - 1;

					for (int i = 0; i < packetsRequired; i++) {
						MC.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(true, false));
					}

					MC.player.networkHandler.sendPacket(
							new PlayerMoveC2SPacket.PositionAndOnGround(newPos.x, newPos.y, newPos.z, true, false));
					MC.player.setPosition(newPos);
					MinecraftClient.getInstance().player.playSound(SoundEvents.ENTITY_ENDERMAN_TELEPORT, 1f,1f);
				}
			}
		}
	}
}
