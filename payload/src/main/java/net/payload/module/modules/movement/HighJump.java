package net.payload.module.modules.movement;

import net.payload.module.Category;
import net.payload.module.Module;
import net.payload.settings.types.FloatSetting;

public class HighJump extends Module {

	private FloatSetting multiplier = FloatSetting.builder().id("highjump_jumpmultiplier")
			.displayName("Jump Multiplier").description("The height that the player will jump.").defaultValue(1.5f)
			.minValue(0.1f).maxValue(10f).step(0.1f).build();

	public HighJump() {
		super("HighJump");
		this.setCategory(Category.of("Movement"));
		this.setDescription("Increases jump height");

		this.addSetting(multiplier);
	}

	@Override
	public void onDisable() {
	}

	@Override
	public void onEnable() {
	}

	@Override
	public void onToggle() {
	}

	public float getJumpHeightMultiplier() {
		return multiplier.getValue();
	}
}