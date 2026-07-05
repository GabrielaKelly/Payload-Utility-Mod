package net.payload.module.modules.world;

import net.minecraft.block.BlockState;
import net.payload.Payload;
import net.payload.event.events.BreakBlockEvent;
import net.payload.event.events.PlaceBlockEvent;
import net.payload.event.listeners.BreakBlockListener;
import net.payload.event.listeners.PlaceBlockListener;
import net.payload.event.listeners.TickListener;
import net.payload.module.Category;
import net.payload.module.Module;
import net.payload.settings.types.BooleanSetting;

public class NoGhostBlocks extends Module implements BreakBlockListener, PlaceBlockListener {

	private BooleanSetting breaking = BooleanSetting.builder()
			.id("noghostblocks_triggerbreaking")
			.displayName("Breaking")
			.defaultValue(true)
			.build();

	public BooleanSetting placing = BooleanSetting.builder()
			.id("noghostblocks_triggerplacing")
			.displayName("Placing")
			.defaultValue(true)
			.build();

	public NoGhostBlocks() {
		super("NoGhostBlocks");
		this.setCategory(Category.of("World"));
		this.setDescription("Syncs block placements from the server to the clientside");

		this.addSetting(breaking);
		this.addSetting(placing);
	}

	@Override
	public void onDisable() {
		Payload.getInstance().eventManager.RemoveListener(BreakBlockListener.class, this);
		Payload.getInstance().eventManager.RemoveListener(PlaceBlockListener.class, this);
	}

	@Override
	public void onEnable() {
		Payload.getInstance().eventManager.AddListener(BreakBlockListener.class, this);
		Payload.getInstance().eventManager.AddListener(PlaceBlockListener.class, this);
	}

	@Override
	public void onToggle() {

	}

	@Override
	public void onBreak(BreakBlockEvent event) {
		if (nullCheck()) return;

		if (MC.isInSingleplayer() || !breaking.get()) return;

		if (event.blockPos != null) {
			event.cancel();
			BlockState blockState = MC.world.getBlockState(event.blockPos);
			blockState.getBlock().onBreak(MC.world, event.blockPos, blockState, MC.player);
		}
	}

	@Override
	public void onPlace(PlaceBlockEvent event) {
		if (nullCheck()) return;

		if (!placing.get()) return;

		event.cancel();
	}
}
