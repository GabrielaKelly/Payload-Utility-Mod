package net.payload.module.modules.render;

import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.payload.Payload;
import net.payload.event.events.Render3DEvent;
import net.payload.event.events.TickEvent;
import net.payload.event.listeners.Render3DListener;
import net.payload.event.listeners.TickListener;
import net.payload.gui.colors.Color;
import net.payload.module.Category;
import net.payload.module.Module;
import net.payload.settings.SettingGroup;
import net.payload.settings.SettingManager;
import net.payload.settings.types.BooleanSetting;
import net.payload.settings.types.ColorSetting;
import net.payload.settings.types.EnumSetting;
import net.payload.settings.types.FloatSetting;
import net.payload.utils.Interpolation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.payload.utils.render.Render3D;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class Tracer extends Module implements Render3DListener, TickListener {

	// Setting Groups
	private final SettingGroup generalSettings;
	private final SettingGroup targetSettings;
	private final SettingGroup colorSettings;

	// General Settings
	private final FloatSetting lineWidth;
	private final FloatSetting maxLines;
	private final EnumSetting<TracerTarget> target;
	private final EnumSetting<TracerMode> mode;

	// Target Settings
	private final BooleanSetting showPlayers;
	private final BooleanSetting showPassives;
	private final BooleanSetting showHostiles;
	private final BooleanSetting showItems;
	private final BooleanSetting showMisc;

	// Color Settings
	private final ColorSetting colorPlayer;
	private final ColorSetting colorPassive;
	private final ColorSetting colorHostile;
	private final ColorSetting colorItem;
	private final ColorSetting colorMisc;

	// Entity list for rendering
	private List<Entity> sorted = new ArrayList<>();

	public enum TracerMode {
		Stem, Fill
	}

	public enum TracerTarget {
		Head, Body, Feet
	}

	public Tracer() {
		super("Tracer");
		this.setCategory(Category.of("Render"));
		this.setDescription("Points toward other players and entities with a line.");

		// Initialize setting groups
		generalSettings = SettingGroup.Builder.builder()
				.id("tracer_general")
				.displayName("General")
				.description("General tracer settings")
				.build();

		targetSettings = SettingGroup.Builder.builder()
				.id("tracer_targets")
				.displayName("Targets")
				.description("Entity targeting options")
				.build();

		colorSettings = SettingGroup.Builder.builder()
				.id("tracer_colors")
				.displayName("Colors")
				.description("Entity color settings")
				.build();

		// General Settings
		lineWidth = FloatSetting.builder()
				.id("tracer_line_width")
				.displayName("Line Width")
				.description("Width of the tracer lines")
				.defaultValue(1f)
				.minValue(0.1f)
				.maxValue(10f)
				.step(0.1f)
				.build();

		maxLines = FloatSetting.builder()
				.id("tracer_max_lines")
				.displayName("Max Lines")
				.description("The maximum amount of lines that can be rendered at once")
				.defaultValue(100f)
				.minValue(1f)
				.maxValue(300f)
				.step(1f)
				.build();

		target = EnumSetting.<TracerTarget>builder()
				.id("tracer_target")
				.displayName("Tracer Target")
				.description("The part of the body the tracer will target")
				.defaultValue(TracerTarget.Head)
				.build();

		mode = EnumSetting.<TracerMode>builder()
				.id("tracer_mode")
				.displayName("Tracer Mode")
				.description("The tracer rendering mode")
				.defaultValue(TracerMode.Stem)
				.build();

		// Target Settings
		showPlayers = BooleanSetting.builder()
				.id("tracer_show_players")
				.displayName("Players")
				.description("Show tracers for players")
				.defaultValue(true)
				.build();

		showPassives = BooleanSetting.builder()
				.id("tracer_show_passives")
				.displayName("Passives")
				.description("Show tracers for passive mobs like animals and villagers")
				.defaultValue(true)
				.build();

		showHostiles = BooleanSetting.builder()
				.id("tracer_show_hostiles")
				.displayName("Hostiles")
				.description("Show tracers for hostile mobs")
				.defaultValue(true)
				.build();

		showItems = BooleanSetting.builder()
				.id("tracer_show_items")
				.displayName("Items")
				.description("Show tracers for dropped items")
				.defaultValue(false)
				.build();

		showMisc = BooleanSetting.builder()
				.id("tracer_show_misc")
				.displayName("Misc")
				.description("Show tracers for miscellaneous entities")
				.defaultValue(false)
				.build();

		// Color Settings
		colorPlayer = ColorSetting.builder()
				.id("tracer_color_player")
				.displayName("Player Color")
				.description("Color for player tracers")
				.defaultValue(new Color(1, 0f, 0f, 0.3f)) // Red
				.build();

		colorPassive = ColorSetting.builder()
				.id("tracer_color_passive")
				.displayName("Passive Color")
				.description("Color for passive mob tracers")
				.defaultValue(new Color(0f, 1f, 0f, 0.3f)) // Green
				.build();

		colorHostile = ColorSetting.builder()
				.id("tracer_color_hostile")
				.displayName("Hostile Color")
				.description("Color for hostile mob tracers")
				.defaultValue(new Color(255, 255, 0, 100)) // Yellow
				.build();

		colorItem = ColorSetting.builder()
				.id("tracer_color_item")
				.displayName("Item Color")
				.description("Color for item tracers")
				.defaultValue(new Color(1, 0f, 1f, 0.3f)) // Purple
				.build();

		colorMisc = ColorSetting.builder()
				.id("tracer_color_misc")
				.displayName("Misc Color")
				.description("Color for miscellaneous entity tracers")
				.defaultValue(new Color(0, 0f, 1f, 0.3f)) // Cyan
				.build();

		// Add settings to general group
		generalSettings.addSetting(lineWidth);
		generalSettings.addSetting(maxLines);
		generalSettings.addSetting(target);
		generalSettings.addSetting(mode);

		// Add settings to target group
		targetSettings.addSetting(showPlayers);
		targetSettings.addSetting(showPassives);
		targetSettings.addSetting(showHostiles);
		targetSettings.addSetting(showItems);
		targetSettings.addSetting(showMisc);

		// Add settings to color group
		colorSettings.addSetting(colorPlayer);
		colorSettings.addSetting(colorPassive);
		colorSettings.addSetting(colorHostile);
		colorSettings.addSetting(colorItem);
		colorSettings.addSetting(colorMisc);

		// Add groups to module
		this.addSetting(generalSettings);
		this.addSetting(targetSettings);
		this.addSetting(colorSettings);

		// Register settings with SettingManager
		registerAllSettings();
	}

	private void registerAllSettings() {
		// Register groups
		SettingManager.registerSetting(generalSettings);
		SettingManager.registerSetting(targetSettings);
		SettingManager.registerSetting(colorSettings);

		// Register general settings
		SettingManager.registerSetting(lineWidth);
		SettingManager.registerSetting(maxLines);
		SettingManager.registerSetting(target);
		SettingManager.registerSetting(mode);

		// Register target settings
		SettingManager.registerSetting(showPlayers);
		SettingManager.registerSetting(showPassives);
		SettingManager.registerSetting(showHostiles);
		SettingManager.registerSetting(showItems);
		SettingManager.registerSetting(showMisc);

		// Register color settings
		SettingManager.registerSetting(colorPlayer);
		SettingManager.registerSetting(colorPassive);
		SettingManager.registerSetting(colorHostile);
		SettingManager.registerSetting(colorItem);
		SettingManager.registerSetting(colorMisc);
	}

	@Override
	public void onDisable() {
		Payload.getInstance().eventManager.RemoveListener(Render3DListener.class, this);
		Payload.getInstance().eventManager.RemoveListener(TickListener.class, this);
	}

	@Override
	public void onEnable() {
		Payload.getInstance().eventManager.AddListener(Render3DListener.class, this);
		Payload.getInstance().eventManager.AddListener(TickListener.class, this);
	}

	@Override
	public void onToggle() {
		// No action needed
	}

	@Override
	public void onRender(Render3DEvent event) {

		if (nullCheck()) return;
		if (!MC.options.getPerspective().isFirstPerson()) return;

		boolean viewBobbing = MC.options.getBobView().getValue();
		MC.options.getBobView().setValue(false);

		Entity renderEntity = MC.getCameraEntity() == null ? MC.player : MC.getCameraEntity();

		int lineCount = 0;

		for (Entity entity : sorted) {
			if (lineCount >= maxLines.getValue()) {
				break;
			}

			// Skip if entity is null or should not be processed
			if (entity == null || shouldSkip(entity)) {
				continue;
			}

			// Get color for this entity type
			Color color = getColorForEntity(entity);

			// Skip if entity should not be rendered based on filter settings
			if (color == null) {
				continue;
			}

			Vec3d interpolation = Interpolation.interpolateEntity(entity);
			double x = interpolation.getX();
			double y = interpolation.getY();
			double z = interpolation.getZ();

			Box bb;

			// Determine the bounding box based on target setting
			if (target.getValue() == TracerTarget.Head) {
				bb = new Box(x - 0.25, y + entity.getHeight() - 0.45, z - 0.25,
						x + 0.25, y + entity.getHeight() + 0.055, z + 0.25);
			} else {
				bb = new Box(x - 0.4, y, z - 0.4,
						x + 0.4, y + entity.getHeight() + 0.18, z + 0.4);
			}

			float distance = renderEntity.distanceTo(entity);

			// Distance-based fade effect
			float alpha = Math.max(0, Math.min(1, 1 - (distance / 120f)));
			Color adjustedColor = new Color(
					color.getRed(),
					color.getGreen(),
					color.getBlue(),
					(int)(color.getAlpha() * alpha)
			);

			final Vec3d rotation = new Vec3d(0, 0, 75)
					.rotateX(-(float) Math.toRadians(renderEntity.getPitch()))
					.rotateY(-(float) Math.toRadians(renderEntity.getYaw()))
					.add(renderEntity.getEyePos());

			// Draw stem tracer if enabled
			if (mode.getValue() == TracerMode.Stem) {
				Render3D.drawLine3D(
						event.GetMatrix(),
						event.getCamera(),
						new Vec3d(x, y, z),
						new Vec3d(x, renderEntity.getHeight() + y, z),
						adjustedColor,
						lineWidth.getValue()
				);
			}

			Vec3d start = new Vec3d(rotation.x, rotation.y, rotation.z);
			Vec3d end;

			// Determine target position based on setting
			switch (target.getValue()) {
				case Head:
					end = new Vec3d(x, y + entity.getHeight() - 0.18f, z);
					break;
				case Body:
					end = new Vec3d(x, y + entity.getHeight() / 2.0f, z);
					break;
				case Feet:
				default:
					end = new Vec3d(x, y, z);
					break;
			}

			// Draw the main tracer line
			Render3D.drawLine3D(
					event.GetMatrix(),
					event.getCamera(),
					start,
					end,
					adjustedColor,
					lineWidth.getValue()
			);

			// Draw box around entity if Fill mode is enabled
			if (mode.getValue() == TracerMode.Fill) {
				Color fillColor = new Color(
						adjustedColor.getRed(),
						adjustedColor.getGreen(),
						adjustedColor.getBlue(),
						78
				);
				Render3D.draw3DBox(
						event.GetMatrix(),
						event.getCamera(),
						bb,
						fillColor,
						lineWidth.getValue()
				);
			}

			lineCount++;
		}

		MC.options.getBobView().setValue(viewBobbing);
	}

	/**
	 * Determines if an entity should be skipped for processing
	 */
	private boolean shouldSkip(Entity entity) {
		// Skip self
		if (entity == MC.player) return true;

		// Skip camera entity in first person
		if (entity == MC.cameraEntity && MC.options.getPerspective().isFirstPerson()) return true;

		// Skip vehicle of the camera entity
		if (MC.getCameraEntity() != null && entity.equals(MC.getCameraEntity().getRootVehicle())) return true;

		return false;
	}

	/**
	 * Gets the appropriate color for an entity based on its type and filter settings
	 * Returns null if the entity should not be rendered
	 */
	private Color getColorForEntity(Entity entity) {
		// Players
		if (entity instanceof PlayerEntity) {
			return showPlayers.getValue() ? colorPlayer.getValue() : null;
		}

		// Items and item frames
		if (entity instanceof ItemEntity || entity instanceof ItemFrameEntity) {
			return showItems.getValue() ? colorItem.getValue() : null;
		}

		// Passive mobs (animals and villagers)
		if (entity instanceof AnimalEntity || entity instanceof VillagerEntity) {
			return showPassives.getValue() ? colorPassive.getValue() : null;
		}

		// Hostile mobs
		if (entity instanceof Monster) {
			return showHostiles.getValue() ? colorHostile.getValue() : null;
		}

		// Miscellaneous entities (anything else that's living)
		if (entity instanceof LivingEntity) {
			return showMisc.getValue() ? colorMisc.getValue() : null;
		}

		// Other non-living entities that aren't items
		if (!(entity instanceof ItemEntity || entity instanceof ItemFrameEntity)) {
			return showMisc.getValue() ? colorMisc.getValue() : null;
		}

		return null;
	}

	@Override
	public void onTick(TickEvent.Pre event) {
		// Get all entities and sort by distance
		sorted = Payload.getInstance().entityManager.getEntities();

		try {
			sorted.sort(Comparator.comparingDouble(entity -> {
				if (MC.player == null) return Double.MAX_VALUE;
				return MC.player.squaredDistanceTo(entity);
			}));
		} catch (IllegalStateException ignored) {
			// Handle potential sorting errors silently
		}
	}

	@Override
	public void onTick(TickEvent.Post event) {
		// No action needed
	}
}