

/**
 * PlayerESP Module
 */
package net.payload.module.modules.render;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.payload.Payload;
import net.payload.event.events.Render2DEvent;
import net.payload.event.events.Render3DEvent;
import net.payload.event.listeners.Render2DListener;
import net.payload.event.listeners.Render3DListener;
import net.payload.gui.colors.Color;
import net.payload.module.Category;
import net.payload.module.Module;
import net.payload.settings.types.BooleanSetting;
import net.payload.settings.types.ColorSetting;
import net.payload.settings.types.EnumSetting;
import net.payload.settings.types.FloatSetting;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.payload.utils.render.Render3D;
import net.payload.utils.render.TextUtils;

public class PlayerESP extends Module implements Render3DListener {

	public enum Mode {
		BoundingBox, Glow
	}

	public final EnumSetting<PlayerESP.Mode> mode = EnumSetting.<PlayerESP.Mode>builder().id("playeresp_draw_mode")
			.displayName("Mode").description("Mode").defaultValue(Mode.Glow).build();

	public ColorSetting color_default = ColorSetting.builder().id("playeresp_color_default")
			.displayName("Default Color").description("Default Color").defaultValue(new Color(255, 0, 0, 100)).build();

	public ColorSetting color_friendly = ColorSetting.builder().id("playeresp_color_friendly")
			.displayName("Friendly Color").description("Friendly Color").defaultValue(new Color(0, 255, 0, 100)).build();

	private FloatSetting lineThickness = FloatSetting.builder().id("playeresp_linethickness")
			.displayName("Line Thickness").description("Adjust the thickness of the ESP box lines").defaultValue(1f)
			.minValue(0f).maxValue(5f).step(0.1f).build();

	public BooleanSetting antiInvis = BooleanSetting.builder().id("playeresp_antiinvis").displayName("Anti Invis")
			.description("No invisible people").defaultValue(true).build();

	public PlayerESP() {
		super("PlayerESP");
		this.setCategory(Category.of("Render"));
		this.setDescription("Allows the player to see other players through walls");

		this.addSetting(mode);
		this.addSetting(color_default);
		this.addSetting(color_friendly);
		this.addSetting(lineThickness);
	}

	@Override
	public void onDisable() {
		Payload.getInstance().eventManager.RemoveListener(Render3DListener.class, this);
	}

	@Override
	public void onEnable() {
		Payload.getInstance().eventManager.AddListener(Render3DListener.class, this);
	}

	@Override
	public void onToggle() {

	}

	@Override
	public void onRender(Render3DEvent event) {
		if (mode.getValue() == PlayerESP.Mode.BoundingBox) {
			for (AbstractClientPlayerEntity entity : MC.world.getPlayers()) {
				if (entity != MC.player) {
					if (Payload.getInstance().friendsList.contains((PlayerEntity) entity)) {
						Render3D.draw3DBox(event.GetMatrix(), event.getCamera(), entity.getBoundingBox(), color_friendly.getValue(),
								lineThickness.getValue().floatValue());
					}
					else {
						Render3D.draw3DBox(event.GetMatrix(), event.getCamera(), entity.getBoundingBox(), color_default.getValue(),
								lineThickness.getValue().floatValue());
					}
				}
			}
		}
	}

	//2D rendering that may or may not be added
	/*
	@Override
	public void onRender(Render2DEvent event) {
		DrawContext context = event.getDrawContext();

		if (mode.getValue() == Mode.Flat) {
			for (AbstractClientPlayerEntity ent : MC.world.getPlayers()) {
				if (ent != MC.player) {

					// Interpolate position
					double tickDelta = event.getRenderTickCounter().getTickDelta(false);
					double x = ent.prevX + (ent.getX() - ent.prevX) * tickDelta;
					double y = ent.prevY + (ent.getY() - ent.prevY) * tickDelta;
					double z = ent.prevZ + (ent.getZ() - ent.prevZ) * tickDelta;

					Vec3d worldPos = new Vec3d(x, y + ent.getStandingEyeHeight(), z); // Use eye height for better placement
					Vec3d screenPos = TextUtils.worldSpaceToScreenSpace(worldPos);

					if (screenPos == null) continue; // Skip if off-screen

					float dist = (float) Math.cbrt(MC.cameraEntity.squaredDistanceTo(worldPos));
					float scaleVal = Math.max((1 - dist * 0.01f), 1);

					context.getMatrices().scale(scaleVal, scaleVal, 1);

					int boxWidth = 10;
					int boxHeight = 20;

					int x1 = (int) (screenPos.x - boxWidth / 2.0);
					int y1 = (int) (screenPos.y);
					int x2 = x1 + boxWidth;
					int y2 = y1 + boxHeight;

					int color = Payload.getInstance().friendsList.contains(ent)
							? color_friendly.getValue().getColorAsInt()
							: color_default.getValue().getColorAsInt();

					context.fill(x1, y1, x2, y2, color);
				}
			}
		}
	}

	 */
}
