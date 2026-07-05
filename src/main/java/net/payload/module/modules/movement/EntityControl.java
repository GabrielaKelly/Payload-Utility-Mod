package net.payload.module.modules.movement;

import net.payload.Payload;
import net.payload.event.events.TickEvent.Post;
import net.payload.event.events.TickEvent.Pre;
import net.payload.event.listeners.TickListener;
import net.payload.interfaces.IHorseBaseEntity;
import net.payload.mixin.ClientPlayerEntityAccessor;
import net.payload.module.Category;
import net.payload.module.Module;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.payload.settings.types.BooleanSetting;

public class EntityControl extends Module implements TickListener {

	private BooleanSetting maxjump = BooleanSetting.builder()
			.id("entitycontrol_maxjump")
			.displayName("max jump")
			.defaultValue(true)
			.build();

	private BooleanSetting forceSprint = BooleanSetting.builder()
			.id("entitycontrol_sprint")
			.displayName("force sprint")
			.defaultValue(true)
			.build();


	public EntityControl() {
		super("EntityControl");
		this.setDescription("Allows greater control over ALREADY SADDLED entities");
		this.setCategory(Category.of("Movement"));
		this.addSetting(maxjump);
		this.addSetting(forceSprint);
	}


	@Override
	public void onDisable() {
		Payload.getInstance().eventManager.RemoveListener(TickListener.class, this);

		for (Entity entity : MC.world.getEntities()) {
			if (entity instanceof AbstractHorseEntity)
				((IHorseBaseEntity) entity).setSaddled(false);
		}
	}

	@Override
	public void onEnable() {
		Payload.getInstance().eventManager.AddListener(TickListener.class, this);
	}

	@Override
	public void onToggle() {

	}

	@Override
	public void onTick(Pre event) {
		if (nullCheck()) return;

		for (Entity entity : MC.world.getEntities()) {
			if (entity instanceof AbstractHorseEntity) ((IHorseBaseEntity) entity).setSaddled(true);
		}

		if (maxjump.getValue()) ((ClientPlayerEntityAccessor) MC.player).setMountJumpStrength(1);

		if (forceSprint.getValue()) ((ClientPlayerEntityAccessor) MC.player).invokeCanSprint();
	}

	@Override
	public void onTick(Post event) {
	}
}
