package net.payload.module.modules.movement;

import net.payload.module.Category;
import net.payload.module.Module;

public class PortalGui extends Module {
    public PortalGui() {
        super("PortalGui");
        this.setCategory(Category.of("Movement"));
        this.setDescription("Allows you to open GUIs while in a nether portal");
    }

    @Override
    public void onEnable() {}
    @Override
    public void onDisable() {}
    @Override
    public void onToggle() {}
}