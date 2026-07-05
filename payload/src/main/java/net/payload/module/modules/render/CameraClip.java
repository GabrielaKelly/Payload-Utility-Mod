package net.payload.module.modules.render;

import net.payload.Payload;
import net.payload.event.events.Render3DEvent;
import net.payload.event.listeners.Render3DListener;
import net.payload.module.Category;
import net.payload.module.Module;
import net.minecraft.client.option.Perspective;
import net.payload.settings.types.BooleanSetting;
import net.payload.settings.types.FloatSetting;

public class CameraClip extends Module implements Render3DListener {

	private final FloatSetting distance = FloatSetting.builder()
			.id("camera_distance")
			.displayName("Distance")
			.description("Camera distance in blocks")
			.defaultValue(4.0f)
			.minValue(1.0f)
			.maxValue(20.0f)
			.step(1f)
			.build();

	private final BooleanSetting noFront = BooleanSetting.builder()
			.id("no_front")
			.displayName("No Front")
			.description("Prevents front view camera")
			.defaultValue(false)
			.build();

	private boolean firstPerson = false;
	private long lastTransitionTime = 0;

	public CameraClip() {
		super("CameraClip");
		this.setCategory(Category.of("Render"));
		this.setDescription("Allows your camera to clip through blocks");

		// Register settings
		this.addSetting(distance);
		this.addSetting(noFront);
	}

	@Override
	public void onRender(Render3DEvent event) {
		if (nullCheck()) return;

		if (MC.options.getPerspective() == Perspective.THIRD_PERSON_FRONT && noFront.getValue()) {
			MC.options.setPerspective(Perspective.FIRST_PERSON);
		}

		// Handle perspective transitions
		boolean currentFirstPerson = MC.options.getPerspective() == Perspective.FIRST_PERSON;
		if (currentFirstPerson != firstPerson) {
			firstPerson = currentFirstPerson;
			lastTransitionTime = System.currentTimeMillis();
		}
	}

	public float getDistance() {
		long currentTime = System.currentTimeMillis();
		long elapsedTime = currentTime - lastTransitionTime;
		float progress = Math.min(1.0f, elapsedTime);

		// Quadratic easing
		float quad = firstPerson ? (1 - (progress * progress)) : (progress * progress);
		return (float) (1.0 + ((distance.getValue() - 1.0) * quad));
	}

	@Override
	public void onEnable() {
		Payload.getInstance().eventManager.AddListener(Render3DListener.class, this);
		lastTransitionTime = System.currentTimeMillis();
	}

	@Override
	public void onDisable() {
		Payload.getInstance().eventManager.RemoveListener(Render3DListener.class, this);
	}

	@Override
	public void onToggle() {
	}
}