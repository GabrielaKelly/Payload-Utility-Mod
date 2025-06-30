package net.payload.module.modules.render;

import net.payload.module.Category;
import net.payload.module.Module;
import net.payload.settings.types.BooleanSetting;

public class Tooltips extends Module {

	public BooleanSetting storage = BooleanSetting.builder().id("tooltips_storage").displayName("Storage")
			.description("Renders the contents of the storage item.").defaultValue(true).build();

	public BooleanSetting maps = BooleanSetting.builder().id("tooltips_maps").displayName("Maps")
			.description("Render a map preview").defaultValue(true).build();

	public Tooltips() {
		super("Tooltips");
		this.setCategory(Category.of("Render"));
		this.setDescription("Renders previews of what maps and storage blocks contain");

		this.addSetting(storage);
		this.addSetting(maps);
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

	public Boolean getStorage() {
		return this.storage.getValue();
	}

	public boolean getMap() {
		return this.maps.getValue();
	}
}
