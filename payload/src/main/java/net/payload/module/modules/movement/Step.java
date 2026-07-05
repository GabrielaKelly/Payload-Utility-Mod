
/**
 * Step Module
 */
package net.payload.module.modules.movement;

import net.payload.module.Category;
import net.payload.module.Module;
import net.payload.settings.types.FloatSetting;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;

public class Step extends Module {

	private FloatSetting stepHeight = FloatSetting.builder().id("step_height").displayName("Max Height")
			.description("Height that the player will step up.").defaultValue(1f).minValue(0f).maxValue(2f).step(0.5f)
			.build();

	public Step() {
		super("Step");
		this.setCategory(Category.of("Movement"));
		this.setDescription("Teleports the player on top of blocks, which is far more convenient than jumping");

		stepHeight.addOnUpdate((i) -> {
			if (state.getValue()) {
				EntityAttributeInstance attribute = MC.player.getAttributeInstance(EntityAttributes.STEP_HEIGHT);
				attribute.setBaseValue(stepHeight.getValue());
			}
		});

		this.addSetting(stepHeight);
	}

	@Override
	public void onDisable() {
		if (MC.player != null) {
			EntityAttributeInstance attribute = MC.player.getAttributeInstance(EntityAttributes.STEP_HEIGHT);
			attribute.setBaseValue(0.5f);
		}
	}

	@Override
	public void onEnable() {
		EntityAttributeInstance attribute = MC.player.getAttributeInstance(EntityAttributes.STEP_HEIGHT);
		attribute.setBaseValue(stepHeight.getValue());
	}

	@Override
	public void onToggle() {

	}

	public float getStepHeight() {
		return stepHeight.getValue();
	}

	public void setStepHeight(float height) {
		this.stepHeight.setValue(height);
	}
}
