package net.payload.module.modules.misc;

import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.SwordItem;
import net.payload.Payload;
import net.payload.event.events.TickEvent;
import net.payload.event.listeners.TickListener;
import net.payload.module.Category;
import net.payload.module.Module;
import net.payload.settings.types.BooleanSetting;
import net.payload.settings.types.FloatSetting;


public class InteractTweaks extends Module implements TickListener {

	public BooleanSetting antiCactus = BooleanSetting.builder()
			.id("interacttweaks_anticactus")
			.displayName("AntiCactus")
			.defaultValue(false)
			.build();

	public BooleanSetting noEntityTrace = BooleanSetting.builder()
			.id("interacttweaks_noentitytrace")
			.displayName("No Entity Trace")
			.defaultValue(false)
			.build();

	public BooleanSetting MultiTask = BooleanSetting.builder()
			.id("interacttweaks_multitask")
			.displayName("MultiTask")
			.defaultValue(false)
			.build();

	public BooleanSetting noAbort = BooleanSetting.builder()
			.id("interacttweaks_noabort")
			.displayName("NoMineAbort")
			.defaultValue(true)
			.build();

	public BooleanSetting noReset = BooleanSetting.builder()
			.id("interacttweaks_noreset")
			.displayName("NoMineReset")
			.defaultValue(true)
			.build();

	public BooleanSetting pickfilter = BooleanSetting.builder()
			.id("interacttweaks_pickfilter")
			.displayName("Pickaxe Only")
			.description("")
			.defaultValue(false)
			.build();

	public BooleanSetting ghostHand = BooleanSetting.builder()
			.id("interacttweaks_ghosthand")
			.displayName("IgnoreBedrock")
			.defaultValue(false)
			.build();

	public BooleanSetting fastUse = BooleanSetting.builder()
			.id("interacttweaks_fastuse")
			.displayName("Fast Use Toggle")
			.defaultValue(true)
			.build();

	public BooleanSetting fastPlace = BooleanSetting.builder()
			.id("interacttweaks_fastplace")
			.displayName("Fast Place Toggle")
			.defaultValue(true)
			.build();

	public FloatSetting fastUseFloat = FloatSetting.builder()
			.id("interacttweaks_fastusefloat")
			.displayName("Fast interact")
			.defaultValue(4f)
			.minValue(0f)
			.maxValue(4f)
			.step(1f)
			.build();

	public BooleanSetting fastBreak = BooleanSetting.builder()
			.id("interacttweaks_fastbreak")
			.displayName("Fast Break Toggle")
			.defaultValue(false)
			.build();

	public FloatSetting fastBreakFloat = FloatSetting.builder()
			.id("interacttweaks_fastbreakfloat")
			.displayName("Fast Break")
			.defaultValue(1.25f)
			.minValue(1.0f)
			.maxValue(10f)
			.step(0.05f)
			.build();

	public boolean isActive;

	public InteractTweaks() {
		super("UseTweaks");

		this.setCategory(Category.of("Misc"));
		this.setDescription("Changes to many player interactions");

		this.addSetting(noEntityTrace);
		this.addSetting(MultiTask);
		this.addSetting(noAbort);
		this.addSetting(noReset);
		this.addSetting(pickfilter);
		this.addSetting(ghostHand);
		this.addSetting(fastUse);
		this.addSetting(fastPlace);
		this.addSetting(fastUseFloat);
		this.addSetting(fastBreak);
		this.addSetting(fastBreakFloat);
		this.addSetting(antiCactus);
	}

	@Override
	public void onDisable() {
		Payload.getInstance().eventManager.RemoveListener(TickListener.class, this);
		isActive = false;
	}

	@Override
	public void onEnable() {
		Payload.getInstance().eventManager.AddListener(TickListener.class, this);
	}

	@Override
	public void onToggle() {
	}

	private boolean isOn() {
		return this.state.get();
	}
	
	public boolean noAbort() {
		return isOn() && noAbort.getValue() && !MC.options.useKey.isPressed();
	}

	public boolean noReset() {
		return isOn() && noReset.getValue();
	}

	public boolean multiTask() {
		return isOn() && MultiTask.getValue();
	}

	public boolean noEntityTrace() {
		if (!this.state.getValue() || !noEntityTrace.getValue()) return false;

		if (pickfilter.getValue()) {
			return MC.player.getMainHandStack().getItem() instanceof PickaxeItem || MC.player.isUsingItem() && !(MC.player.getMainHandStack().getItem() instanceof SwordItem);
		}
		return true;
	}

	public boolean ghostHand() {
		return isOn() && ghostHand.getValue() && !MC.options.useKey.isPressed() && !MC.options.sneakKey.isPressed();
	}

	@Override
	public void onTick(TickEvent.Pre event) {
		Item currentItem = MC.player.getMainHandStack().getItem();
		boolean isBlockItem = currentItem instanceof BlockItem;

		// Fast Place: Apply to block items only
		if (fastPlace.getValue() && isBlockItem) {
			IMC.setItemUseCooldown(Math.round(fastUseFloat.get()));
			return;
		}

		// Fast Use: Apply to non-block items only
		if (fastUse.getValue() && !isBlockItem) {
			IMC.setItemUseCooldown(Math.round(fastUseFloat.get()));
		}
	}

	@Override
	public void onTick(TickEvent.Post event) {

	}
}