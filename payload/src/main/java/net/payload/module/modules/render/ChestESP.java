package net.payload.module.modules.render;

import net.minecraft.block.entity.*;
import net.minecraft.util.math.Box;
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
import net.payload.settings.types.FloatSetting;
import net.payload.utils.ModuleUtils;
import net.payload.utils.render.Render3D;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ChestESP extends Module implements Render3DListener {
    private final SettingGroup toggleSettings;
    private final SettingGroup colorSettings;

    // Boolean Settings
    private final BooleanSetting showChests = BooleanSetting.builder()
            .id("chestesp_show_chests")
            .displayName("Chests")
            .defaultValue(true)
            .build();

    private final BooleanSetting showTrappedChests = BooleanSetting.builder()
            .id("chestesp_show_trapped_chests")
            .displayName("TrappedChests")
            .defaultValue(true)
            .build();

    private final BooleanSetting showEnderChests = BooleanSetting.builder()
            .id("chestesp_show_ender_chests")
            .displayName("EnderChests")
            .defaultValue(true)
            .build();

    private final BooleanSetting showShulkers = BooleanSetting.builder()
            .id("chestesp_show_shulkers")
            .displayName("Shulkers")
            .defaultValue(true)
            .build();

    private final BooleanSetting showHoppers = BooleanSetting.builder()
            .id("chestesp_show_hoppers")
            .displayName("Hoppers")
            .defaultValue(true)
            .build();

    private final BooleanSetting showSmokers = BooleanSetting.builder()
            .id("chestesp_show_smokers")
            .displayName("Smokers")
            .defaultValue(true)
            .build();

    private final BooleanSetting showBlastFurnaces = BooleanSetting.builder()
            .id("chestesp_show_blast_furnaces")
            .displayName("BlastFurnaces")
            .defaultValue(true)
            .build();

    private final BooleanSetting showFurnaces = BooleanSetting.builder()
            .id("chestesp_show_furnaces")
            .displayName("Furnaces")
            .defaultValue(true)
            .build();

    private final BooleanSetting showDroppers = BooleanSetting.builder()
            .id("chestesp_show_droppers")
            .displayName("Droppers")
            .defaultValue(true)
            .build();

    private final BooleanSetting showDispensers = BooleanSetting.builder()
            .id("chestesp_show_dispensers")
            .displayName("Dispensers")
            .defaultValue(true)
            .build();

    // Color Settings
    private final ColorSetting chestColor = ColorSetting.builder()
            .id("chestesp_chest_color")
            .displayName("Chest")
            .defaultValue(new Color(255, 167, 57, 150))
            .build();

    private final ColorSetting trappedChestColor = ColorSetting.builder()
            .id("chestesp_trapped_chest_color")
            .displayName("TrappedChest")
            .defaultValue(new Color(255, 0, 0, 150))
            .build();

    private final ColorSetting enderChestColor = ColorSetting.builder()
            .id("chestesp_ender_chest_color")
            .displayName("EnderChest")
            .defaultValue(new Color(233, 131, 255, 150))
            .build();

    private final ColorSetting shulkerBoxColor = ColorSetting.builder()
            .id("chestesp_shulker_box_color")
            .displayName("ShulkerBox")
            .defaultValue(new Color(131, 200, 255, 150))
            .build();

    private final ColorSetting hopperColor = ColorSetting.builder()
            .id("chestesp_hopper_color")
            .displayName("Hopper")
            .defaultValue(new Color(128, 128, 128, 150))
            .build();

    private final ColorSetting furnaceColor = ColorSetting.builder()
            .id("chestesp_furnace_color")
            .displayName("Furnace")
            .defaultValue(new Color(169, 169, 169, 150))
            .build();

    private final ColorSetting smokerColor = ColorSetting.builder()
            .id("chestesp_smoker_color")
            .displayName("Smoker")
            .defaultValue(new Color(120, 120, 120, 150))
            .build();

    private final ColorSetting blastFurnaceColor = ColorSetting.builder()
            .id("chestesp_blast_furnace_color")
            .displayName("BlastFurnace")
            .defaultValue(new Color(140, 140, 140, 150))
            .build();

    private final ColorSetting dropperColor = ColorSetting.builder()
            .id("chestesp_dropper_color")
            .displayName("Dropper")
            .defaultValue(new Color(105, 105, 105, 150))
            .build();

    private final ColorSetting dispenserColor = ColorSetting.builder()
            .id("chestesp_dispenser_color")
            .displayName("Dispenser")
            .defaultValue(new Color(90, 90, 90, 150))
            .build();

    private final FloatSetting lineThickness = FloatSetting.builder()
            .id("chestesp_linethickness")
            .displayName("Line Thickness")
            .defaultValue(2f)
            .minValue(0f)
            .maxValue(5f)
            .step(0.1f)
            .build();

    private final Map<BlockEntity, Box> previousPositions = new HashMap<>();
    private final Map<BlockEntity, Box> interpolatedPositions = new HashMap<>();

    public ChestESP() {
        super("ChestESP");
        this.setName("StorageESP");
        this.setCategory(Category.of("Render"));
        this.setDescription("Reveals where storage blocks are");
        toggleSettings = SettingGroup.Builder.builder()
                .id("Targets")
                .displayName("Toggles")
                .description("Storage type visibility toggles")
                .build();

        colorSettings = SettingGroup.Builder.builder()
                .id("Colors")
                .displayName("Colors")
                .description("Storage ESP colors")
                .build();

        toggleSettings.addSetting(showChests);
        toggleSettings.addSetting(showTrappedChests);
        toggleSettings.addSetting(showEnderChests);
        toggleSettings.addSetting(showShulkers);
        toggleSettings.addSetting(showHoppers);
        toggleSettings.addSetting(showSmokers);
        toggleSettings.addSetting(showBlastFurnaces);
        toggleSettings.addSetting(showFurnaces);
        toggleSettings.addSetting(showDroppers);
        toggleSettings.addSetting(showDispensers);

        // Add settings to Colors group
        colorSettings.addSetting(chestColor);
        colorSettings.addSetting(trappedChestColor);
        colorSettings.addSetting(enderChestColor);
        colorSettings.addSetting(shulkerBoxColor);
        colorSettings.addSetting(hopperColor);
        colorSettings.addSetting(furnaceColor);
        colorSettings.addSetting(smokerColor);
        colorSettings.addSetting(blastFurnaceColor);
        colorSettings.addSetting(dropperColor);
        colorSettings.addSetting(dispenserColor);

        // Add groups to module
        this.addSetting(toggleSettings);
        this.addSetting(colorSettings);

        // Add uncategorized setting
        this.addSetting(lineThickness);

        // Register all settings with SettingManager
        // Toggle Settings
        SettingManager.registerSetting(showChests);
        SettingManager.registerSetting(showTrappedChests);
        SettingManager.registerSetting(showEnderChests);
        SettingManager.registerSetting(showShulkers);
        SettingManager.registerSetting(showHoppers);
        SettingManager.registerSetting(showSmokers);
        SettingManager.registerSetting(showBlastFurnaces);
        SettingManager.registerSetting(showFurnaces);
        SettingManager.registerSetting(showDroppers);
        SettingManager.registerSetting(showDispensers);

        // Color Settings
        SettingManager.registerSetting(chestColor);
        SettingManager.registerSetting(trappedChestColor);
        SettingManager.registerSetting(enderChestColor);
        SettingManager.registerSetting(shulkerBoxColor);
        SettingManager.registerSetting(hopperColor);
        SettingManager.registerSetting(furnaceColor);
        SettingManager.registerSetting(smokerColor);
        SettingManager.registerSetting(blastFurnaceColor);
        SettingManager.registerSetting(dropperColor);
        SettingManager.registerSetting(dispenserColor);

        // Uncategorized Setting
        SettingManager.registerSetting(lineThickness);

        // Register the groups themselves
        SettingManager.registerSetting(toggleSettings);
        SettingManager.registerSetting(colorSettings);
    }

    @Override
    public void onDisable() {
        Payload.getInstance().eventManager.RemoveListener(Render3DListener.class, this);
        previousPositions.clear();
        interpolatedPositions.clear();
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
        ArrayList<BlockEntity> blockEntities = ModuleUtils.getTileEntities()
                .collect(Collectors.toCollection(ArrayList::new));

        for (BlockEntity blockEntity : blockEntities) {
            Color currentColor = null;
            boolean shouldRender = false;

            if (blockEntity instanceof ChestBlockEntity && showChests.getValue()) {
                currentColor = chestColor.getValue();
                shouldRender = true;
            } else if (blockEntity instanceof TrappedChestBlockEntity && showTrappedChests.getValue()) {
                currentColor = trappedChestColor.getValue();
                shouldRender = true;
            } else if (blockEntity instanceof EnderChestBlockEntity && showEnderChests.getValue()) {
                currentColor = enderChestColor.getValue();
                shouldRender = true;
            } else if (blockEntity instanceof ShulkerBoxBlockEntity && showShulkers.getValue()) {
                currentColor = shulkerBoxColor.getValue();
                shouldRender = true;
            } else if (blockEntity instanceof HopperBlockEntity && showHoppers.getValue()) {
                currentColor = hopperColor.getValue();
                shouldRender = true;
            } else if (blockEntity instanceof FurnaceBlockEntity && showFurnaces.getValue()) {
                currentColor = furnaceColor.getValue();
                shouldRender = true;
            } else if (blockEntity instanceof SmokerBlockEntity && showSmokers.getValue()) {
                currentColor = smokerColor.getValue();
                shouldRender = true;
            } else if (blockEntity instanceof BlastFurnaceBlockEntity && showBlastFurnaces.getValue()) {
                currentColor = blastFurnaceColor.getValue();
                shouldRender = true;
            } else if (blockEntity instanceof DropperBlockEntity && showDroppers.getValue()) {
                currentColor = dropperColor.getValue();
                shouldRender = true;
            } else if (blockEntity instanceof DispenserBlockEntity && showDispensers.getValue()) {
                currentColor = dispenserColor.getValue();
                shouldRender = true;
            }

            if (shouldRender && currentColor != null) {
                renderStorageESP(event, blockEntity, currentColor);
            }
        }
    }

    private void renderStorageESP(Render3DEvent event, BlockEntity blockEntity, Color color) {
        Box currentBox = new Box(blockEntity.getPos());
        Box previousBox = previousPositions.get(blockEntity);

        if (previousBox == null) {
            previousPositions.put(blockEntity, currentBox);
            interpolatedPositions.put(blockEntity, currentBox);
        } else {
            long currentTime = System.nanoTime();
            long deltaTime = currentTime - previousBox.hashCode();
            float partialTicks = (deltaTime / 1000000000.0f) % 1.0f;
            Box interpolatedBox = interpolateBox(previousBox, currentBox, partialTicks);
            interpolatedPositions.put(blockEntity, interpolatedBox);
        }

        Render3D.draw3DBox(event.GetMatrix(), event.getCamera(),
                interpolatedPositions.get(blockEntity),
                color,
                lineThickness.getValue().floatValue());
    }

    private Box interpolateBox(Box previous, Box current, float partialTicks) {
        double x = previous.minX + (current.minX - previous.minX) * partialTicks;
        double y = previous.minY + (current.minY - previous.minY) * partialTicks;
        double z = previous.minZ + (current.minZ - previous.minZ) * partialTicks;
        double x2 = previous.maxX + (current.maxX - previous.maxX) * partialTicks;
        double y2 = previous.maxY + (current.maxY - previous.maxY) * partialTicks;
        double z2 = previous.maxZ + (current.maxZ - previous.maxZ) * partialTicks;
        return new Box(x, y, z, x2, y2, z2);
    }
}