

/**
 * Reach Module
 */
package net.payload.module.modules.combat;

import net.payload.module.Category;
import net.payload.module.Module;
import net.payload.settings.types.FloatSetting;

public class Reach extends Module {

	private FloatSetting distance;

	public Reach() {
		super("Reach");

		this.setCategory(Category.of("Combat"));
		this.setDescription("Allows you to reach comically further");

		distance = FloatSetting.builder().id("reach_distance").displayName("Distance")
				.description("Distance, in blocks, that you can reach.").defaultValue(5f).minValue(1f).maxValue(128f)
				.step(1f).build();
		this.addSetting(distance);
	}

	public float getReach() {
		return distance.getValue().floatValue();
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

	public void setReachLength(float reach) {
		this.distance.setValue(reach);
	}
}