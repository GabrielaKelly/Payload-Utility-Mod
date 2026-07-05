
package net.payload.module.modules.render;

import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.payload.Payload;
import net.payload.event.events.Render3DEvent;
import net.payload.event.listeners.Render3DListener;
import net.payload.gui.colors.Color;
import net.payload.module.Category;
import net.payload.module.Module;
import net.payload.settings.SettingGroup;
import net.payload.settings.SettingManager;
import net.payload.settings.types.BooleanSetting;
import net.payload.settings.types.ColorSetting;
import net.payload.settings.types.EnumSetting;
import net.payload.settings.types.FloatSetting;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.payload.utils.render.Render3D;

import java.util.Objects;

public class EntityESP extends Module implements Render3DListener {
	private final SettingGroup targetSettings;
	private final SettingGroup colorSettings;
	public enum Mode {
		BoundingBox, Glow
	}

	public final EnumSetting<Mode> mode = EnumSetting.<Mode>builder().id("entityesp_draw_mode")
			.displayName("Mode").description("Mode").defaultValue(Mode.Glow).build();

	private ColorSetting color_passive = ColorSetting.builder().id("entityesp_color_passive")
			.displayName("Passive Color").description("Passive Color").defaultValue(new Color(0f, 1f, 0f, 0.3f))
			.build();

	private ColorSetting color_enemies = ColorSetting.builder().id("entityesp_color_enemy").displayName("Enemy Color")
			.description("Enemy Color").defaultValue(new Color(255, 255, 0, 100)).build();

	private ColorSetting color_misc = ColorSetting.builder().id("entityesp_color_misc").displayName("Misc. Color")
			.description("Misc. Color").defaultValue(new Color(0, 0f, 1f, 0.3f)).build();

	private ColorSetting color_item = ColorSetting.builder().id("entityesp_color_item").displayName("Item Color")
			.description("Misc. Color").defaultValue(new Color(1, 0f, 1f, 0.3f)).build();

	public BooleanSetting showPassiveEntities = BooleanSetting.builder().id("entityesp_show_passive")
			.displayName("Passives").description("Show Passive Entities.").defaultValue(true).build();

	public BooleanSetting showEnemies = BooleanSetting.builder().id("entityesp_show_enemies")
			.displayName("Enemies").description("Show Enemies.").defaultValue(true).build();

	public BooleanSetting showItems = BooleanSetting.builder().id("entityesp_show_items")
			.displayName("Items").description("Show Items.").defaultValue(true).build();

	public BooleanSetting showMiscEntities = BooleanSetting.builder().id("entityesp_show_misc")
			.displayName("Misc").description("Show Misc Entities").defaultValue(true).build();

	private FloatSetting lineThickness = FloatSetting.builder().id("entityesp_linethickness")
			.displayName("Line Thickness").description("Adjust the thickness of the ESP box lines").defaultValue(2f)
			.minValue(0f).maxValue(5f).step(0.1f).build();

	public EntityESP() {
		super("EntityESP");
		this.setCategory(Category.of("Render"));
		this.setDescription("Allows the player to see where entities are");
		targetSettings = SettingGroup.Builder.builder()
				.id("Targets")
				.displayName("Targets")
				.description("Entity targeting options")
				.build();

		colorSettings = SettingGroup.Builder.builder()
				.id("Colors")
				.displayName("Colors")
				.description("ESP color configuration")
				.build();
		targetSettings.addSetting(showPassiveEntities);  // Passive
		targetSettings.addSetting(showEnemies);         // Enemy
		targetSettings.addSetting(showMiscEntities);    // Misc
		targetSettings.addSetting(showItems);           // Item

		// Add settings to Colors group
		colorSettings.addSetting(color_passive);        // Passive
		colorSettings.addSetting(color_enemies);        // Enemy
		colorSettings.addSetting(color_misc);           // Misc
		colorSettings.addSetting(color_item);           // Item
		this.addSetting(mode);
		// Add groups to module
		this.addSetting(targetSettings);
		this.addSetting(colorSettings);
		// Add uncategorized settings
		this.addSetting(lineThickness);

		SettingManager.registerSetting(showPassiveEntities);
		SettingManager.registerSetting(showEnemies);
		SettingManager.registerSetting(showMiscEntities);
		SettingManager.registerSetting(showItems);

		// Color Settings
		SettingManager.registerSetting(color_passive);
		SettingManager.registerSetting(color_enemies);
		SettingManager.registerSetting(color_misc);
		SettingManager.registerSetting(color_item);

		// Uncategorized Settings
		SettingManager.registerSetting(mode);
		SettingManager.registerSetting(lineThickness);

		// Register the groups themselves
		SettingManager.registerSetting(targetSettings);
		SettingManager.registerSetting(colorSettings);
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
		if (nullCheck()) return;

		MatrixStack matrixStack = event.GetMatrix();
		float partialTicks = event.getRenderTickCounter().getTickDelta(true);

		for (Entity entity : MC.world.getEntities()) {
			Frustum frustum = event.getFrustum();
			Camera camera = MC.gameRenderer.getCamera();
			Vec3d cameraPosition = camera.getPos();
			if (MC.getEntityRenderDispatcher().shouldRender(entity, frustum, cameraPosition.getX(),
					cameraPosition.getY(), cameraPosition.getZ())) {
				if ((entity instanceof LivingEntity || entity instanceof ItemEntity) && !(entity instanceof PlayerEntity) && entity.getControllingPassenger() != MC.player) {
					Color color = getColorForEntity(entity);
					if (color != null) {

						double interpolatedX = MathHelper.lerp(partialTicks, entity.prevX, entity.getX());
						double interpolatedY = MathHelper.lerp(partialTicks, entity.prevY, entity.getY());
						double interpolatedZ = MathHelper.lerp(partialTicks, entity.prevZ, entity.getZ());

						Box boundingBox = entity.getBoundingBox().offset(interpolatedX - entity.getX(),
								interpolatedY - entity.getY(), interpolatedZ - entity.getZ());

                        if (Objects.requireNonNull(mode.getValue()) == Mode.BoundingBox) {
                            Render3D.draw3DBox(matrixStack, event.getCamera(), boundingBox, color, lineThickness.getValue());
                        }
					}
				}
			}
		}
	}

	public Mode returnmode() {
		return mode.getValue();
	}

	public boolean shouldGlow(Entity entity) {

		if (nullCheck()) return false;

		if (mode.getValue() == Mode.BoundingBox || entity.getControllingPassenger() == MC.player || entity instanceof ItemFrameEntity || entity instanceof PlayerEntity) {
			return false;
		}

		if (entity instanceof AnimalEntity && showPassiveEntities.getValue()) {
			return true; // Passive entities glow
		}

		if (entity instanceof Monster && showEnemies.getValue()) {
			return true; // Enemy entities glow
		}

		if (entity instanceof ItemEntity && showItems.getValue()) {
			return true; // Item entities glow
		}

		if (!(entity instanceof AnimalEntity || entity instanceof Monster || entity instanceof ItemEntity) && showMiscEntities.getValue()) {
			return true; // Miscellaneous entities glow
		}

		return false;
	}


	public boolean shouldSkip(Entity entity) {
		if (entity == MC.player) return true;
		if (entity == MC.cameraEntity && MC.options.getPerspective().isFirstPerson()) return true;
		return false;
	}

	public Color getColor(Entity entity) {
		if (entity instanceof AnimalEntity) {
			return color_passive.getValue();

		} else if (entity instanceof VillagerEntity) {
				return color_passive.getValue();

		} else if (entity instanceof ItemEntity) {
			return color_item.getValue();

		} else if (entity instanceof Monster) {
			return color_enemies.getValue();

		} else if (showMiscEntities.getValue()) {
			return color_misc.getValue();
		}
		else return null;
	}



	private Color getColorForEntity(Entity entity) {
		if (entity instanceof ItemEntity && showItems.getValue()) {
			return color_item.getValue();
		} else if (entity instanceof AnimalEntity && showPassiveEntities.getValue()) {
			return color_passive.getValue();
		} else if (entity instanceof VillagerEntity) {
			return color_passive.getValue();
		} else if (entity instanceof Monster && showEnemies.getValue()) {
			return color_enemies.getValue();
		} else if (!(entity instanceof AnimalEntity || entity instanceof Monster || entity instanceof  ItemEntity || entity instanceof VillagerEntity) && showMiscEntities.getValue()) {
			return color_misc.getValue();
		}
		else return null;
	}

}
