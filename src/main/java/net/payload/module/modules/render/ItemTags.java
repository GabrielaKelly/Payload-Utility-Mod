package net.payload.module.modules.render;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.payload.Payload;
import net.payload.event.events.TickEvent;
import net.payload.event.listeners.TickListener;
import net.payload.module.Category;
import net.payload.module.Module;
import net.payload.settings.types.BooleanSetting;
import net.payload.settings.types.EnumSetting;
import net.payload.settings.types.FloatSetting;
import net.payload.utils.render.ColorBank;
import net.payload.utils.render.FormatBank;

public class ItemTags extends Module implements TickListener {

    private final EnumSetting<FormatBank> format = EnumSetting.<FormatBank>builder()
            .id("itemtag_formatting")
            .displayName("Formatting")
            .defaultValue(FormatBank.BOLD)
            .build();


    private final EnumSetting<ColorBank> frontcolor = EnumSetting.<ColorBank>builder()
            .id("itemtag_colors_name")
            .displayName("Name Color")
            .defaultValue(ColorBank.WHITE)
            .build();

    private final EnumSetting<ColorBank> backcolor = EnumSetting.<ColorBank>builder()
            .id("itemtag_colors_count")
            .displayName("Count Color")
            .defaultValue(ColorBank.WHITE)
            .build();

    private FloatSetting scale = FloatSetting.builder()
            .id("itemtag_scale")
            .displayName("Scale")
            .description("Scale of the NameTags")
            .defaultValue(1.25f)
            .minValue(0f)
            .maxValue(5f)
            .step(0.25f)
            .build();

    private final BooleanSetting customName = BooleanSetting.builder()
            .id("itemtag_customname")
            .displayName("Custom Name")
            .description("Shows the item's custom name if available")
            .defaultValue(true)
            .build();

    private final BooleanSetting count = BooleanSetting.builder()
            .id("itemtag_count")
            .displayName("Show Count")
            .description("Shows the item stack count")
            .defaultValue(true)
            .build();

    private BooleanSetting drawBackground = BooleanSetting.builder()
            .id("itemtag_backdrop")
            .displayName("Show Background")
            .defaultValue(true)
            .build();

    public ItemTags() {
        super("ItemTags");
        this.setCategory(Category.of("Render"));
        this.setDescription("Displays nametags above dropped items, syncs with nametags module");

        this.addSetting(format);
        this.addSetting(frontcolor);    // Add this
        this.addSetting(backcolor);     // Add this
        this.addSetting(scale);
        this.addSetting(customName);
        this.addSetting(count);
    }

    @Override
    public void onDisable() {
        Payload.getInstance().eventManager.RemoveListener(TickListener.class, this);
        resetItemTags();
    }

    @Override
    public void onEnable() {
        Payload.getInstance().eventManager.AddListener(TickListener.class, this);
    }

    @Override
    public void onToggle() {
    }

    @Override
    public void onTick(TickEvent.Pre event) {
        if (nullCheck()) return;
        processItemEntities();
    }

    @Override
    public void onTick(TickEvent.Post event) {
    }

    private void resetItemTags() {
        if (MC.world == null) return;

        for (Entity entity : MC.world.getEntities()) {
            if (entity instanceof ItemEntity itemEntity) {
                itemEntity.setCustomNameVisible(false);
            }
        }
    }

    private void processItemEntities() {
        for (Entity entity : MC.world.getEntities()) {
            if (entity instanceof ItemEntity itemEntity) {
                updateItemTag(itemEntity);
            }
        }
    }

    private void updateItemTag(ItemEntity itemEntity) {
        String itemName = getItemName(itemEntity);
        String countText = getCountText(itemEntity);

        Formatting nameFormatting = Formatting.valueOf(frontcolor.getValue().name());
        Formatting countFormatting = Formatting.valueOf(backcolor.getValue().name());
        Formatting fontSetting = Formatting.valueOf(format.getValue().name());

        // Combine font formatting with color formatting for the item name
        Text formattedName = Text.literal(itemName)
                .formatted(nameFormatting, fontSetting);

        // Apply color formatting to count text
        Text formattedCount = Text.literal(countText)
                .formatted(countFormatting);

        // Combine the formatted components
        Text finalText = formattedName.copy().append(formattedCount);

        itemEntity.setCustomName(finalText);
        itemEntity.setCustomNameVisible(true);
    }

    private String getItemName(ItemEntity itemEntity) {
        return customName.getValue()
                ? itemEntity.getStack().getName().getString()
                : itemEntity.getStack().getItem().getName().getString();
    }

    private String getCountText(ItemEntity itemEntity) {
        return count.getValue()
                ? " x" + itemEntity.getStack().getCount()
                : "";
    }

    public float getNametagScale() {
        return this.scale.getValue();
    }

    public boolean getBackdrop() {
        return drawBackground.get();
    }
}