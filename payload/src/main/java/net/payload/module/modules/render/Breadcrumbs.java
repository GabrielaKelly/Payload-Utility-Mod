

/**
 * Breadcrumbs Module
 */
package net.payload.module.modules.render;

import java.util.LinkedList;

import net.payload.Payload;
import net.payload.event.events.Render3DEvent;
import net.payload.event.events.TickEvent.Post;
import net.payload.event.events.TickEvent.Pre;
import net.payload.event.listeners.Render3DListener;
import net.payload.event.listeners.TickListener;
import net.payload.gui.colors.Color;
import net.payload.module.Category;
import net.payload.module.Module;
import net.payload.settings.types.ColorSetting;
import net.payload.settings.types.FloatSetting;
import net.minecraft.util.math.Vec3d;
import net.payload.utils.render.Render3D;

public class Breadcrumbs extends Module implements Render3DListener, TickListener {

	private ColorSetting color = ColorSetting.builder().id("breadcrumbs_color").displayName("Color")
			.description("Color").defaultValue(new Color(0, 1f, 1f)).build();

	public FloatSetting lineThickness = FloatSetting.builder().id("breadcrumbs_linethickness")
			.displayName("Line Thickness").description("Line Thickness.").defaultValue(1f).minValue(0.1f).maxValue(10f)
			.step(0.1f).build();

	private final float distanceThreshold = 1.0f; // Minimum distance to record a new position
	private float currentTick = 0;
	private float timer = 10;
	private final LinkedList<Vec3d> positions = new LinkedList<>();
	private final int maxPositions = 1000;

	public Breadcrumbs() {
		super("Breadcrumbs");
		this.setCategory(Category.of("Render"));
		this.setDescription("Leaves \"breadcrumbs\" while traveling so you can retrace your steps");
		this.addSetting(color);
		this.addSetting(lineThickness);
	}

	@Override
	public void onDisable() {
		Payload.getInstance().eventManager.RemoveListener(Render3DListener.class, this);
		Payload.getInstance().eventManager.RemoveListener(TickListener.class, this);
		positions.clear();
	}

	@Override
	public void onEnable() {
		Payload.getInstance().eventManager.AddListener(Render3DListener.class, this);
		Payload.getInstance().eventManager.AddListener(TickListener.class, this);
	}

	@Override
	public void onToggle() {

	}

	@Override
	public void onRender(Render3DEvent event) {
		Vec3d prevPosition = null;
		for (Vec3d position : positions) {
			if (prevPosition != null) {
				Render3D.drawLine3D(event.GetMatrix(), event.getCamera(), prevPosition, position, color.getValue(),
						lineThickness.getValue().floatValue());
			}
			prevPosition = position;
		}
	}

	@Override
	public void onTick(Pre event) {

	}

	@Override
	public void onTick(Post event) {
		currentTick++;
		if (timer == currentTick) {
			currentTick = 0;
			if (!Payload.getInstance().moduleManager.freecam.state.getValue()) {
				Vec3d currentPosition = MC.player.getPos();
				if (positions.isEmpty() || positions.getLast().squaredDistanceTo(currentPosition) >= distanceThreshold
						* distanceThreshold) {
					if (positions.size() >= maxPositions) {
						positions.removeFirst();
					}
					positions.add(currentPosition);
				}
			}
		}
	}
}