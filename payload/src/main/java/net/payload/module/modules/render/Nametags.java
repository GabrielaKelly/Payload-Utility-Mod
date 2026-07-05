

/**
 * Nametags Module
 */
package net.payload.module.modules.render;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.payload.Payload;
import net.payload.event.events.Render2DEvent;
import net.payload.event.events.TickEvent;
import net.payload.event.listeners.Render2DListener;
import net.payload.event.listeners.TickListener;
import net.payload.gui.colors.Color;
import net.payload.module.Category;
import net.payload.module.Module;
import net.payload.settings.PageGroup;
import net.payload.settings.SettingGroup;
import net.payload.settings.SettingManager;
import net.payload.settings.types.BooleanSetting;
import net.payload.settings.types.ColorSetting;
import net.payload.settings.types.EnumSetting;
import net.payload.settings.types.FloatSetting;
import net.payload.utils.render.TextUtils;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class Nametags extends Module implements TickListener, Render2DListener {

    private final EnumSetting<Mode> mode = EnumSetting.<Mode>builder()
            .id("nametags_mode")
            .displayName("Mode")
            .description("How armor is displayed on nametags")
            .defaultValue(Mode.Payload)
            .build();

    private final ColorSetting frontcolor = ColorSetting.builder()
            .id("nametags_color")
            .displayName("Color")
            .defaultValue(new Color(255, 255, 255, 255))
            .build();

    private FloatSetting scale = FloatSetting.builder()
    		.id("nametags_scale")
    		.displayName("Scale")
    		.description("Scale of the NameTags")
    		.defaultValue(1.25f)
    		.minValue(0f)
    		.maxValue(5f)
    		.step(0.25f)
    		.build();

    private BooleanSetting unlimitedRange = BooleanSetting.builder()
            .id("nametags_range")
            .displayName("Unlimited Range")
            .description("Range")
            .defaultValue(true)
            .build();

    private BooleanSetting forceShowMobNametags = BooleanSetting.builder()
            .id("nametags_forcemobs")
            .displayName("Force Named Mobs")
            .description("Range")
            .defaultValue(true)
            .build();

    private BooleanSetting forceShowPlayerNametags = BooleanSetting.builder()
            .id("nametags_forceplayernametags")
            .displayName("Force Players")
            .description("Range")
            .defaultValue(true)
            .build();

    private BooleanSetting showUnnamedMobs = BooleanSetting.builder()
            .id("nametags_showunnamedmobs")
            .displayName("Show Unnamed Mobs")
            .description("")
            .defaultValue(false)
            .build();

    private BooleanSetting showOtherEntities = BooleanSetting.builder()
            .id("nametags_forceotherentities")
            .displayName("Force Misc Entities")
            .description("Range")
            .defaultValue(false)
            .build();

    private BooleanSetting showHP = BooleanSetting.builder()
            .id("nametags_hp")
            .displayName("Show HP")
            .description("Range")
            .defaultValue(true)
            .build();

    private BooleanSetting drawBackground = BooleanSetting.builder()
            .id("nametags_backdrop")
            .displayName("Show Background")
            .defaultValue(true)
            .build();

    //NewTags

    private final SettingGroup generalSettings = SettingGroup.Builder.builder()
            .id("nametags_general")
            .displayName("General")
            .description("General nametag settings")
            .build();

    private final SettingGroup displaySettings = SettingGroup.Builder.builder()
            .id("nametags_display")
            .displayName("Display")
            .description("Nametag information display settings")
            .build();

    private final SettingGroup colorSettings = SettingGroup.Builder.builder()
            .id("nametags_color")
            .displayName("Colors")
            .description("Nametag color settings")
            .build();

    // General Settings
    /*
    private final FloatSetting scale = FloatSetting.builder()
            .id("nametags_scale")
            .displayName("Scale")
            .description("Size of the nametags")
            .defaultValue(0.68f)
            .minValue(0.1f)
            .maxValue(2f)
            .step(0.01f)
            .build();

     */

    private final FloatSetting minScale = FloatSetting.builder()
            .id("nametags_min_scale")
            .displayName("Min Scale")
            .description("Minimum size of the nametags at a distance")
            .defaultValue(0.2f)
            .minValue(0.1f)
            .maxValue(1f)
            .step(0.01f)
            .build();

    private final FloatSetting scaled = FloatSetting.builder()
            .id("nametags_scaled")
            .displayName("DistScale")
            .description("Scale factor for distance scaling")
            .defaultValue(1f)
            .minValue(0f)
            .maxValue(2f)
            .step(0.1f)
            .build();

    private final FloatSetting height = FloatSetting.builder()
            .id("nametags_height")
            .displayName("Height")
            .description("Additional height adjustment")
            .defaultValue(0f)
            .minValue(-3f)
            .maxValue(3f)
            .step(0.1f)
            .build();

    // Display Settings
    private final BooleanSetting god = BooleanSetting.builder()
            .id("nametags_god")
            .displayName("God")
            .description("Show if player is in god mode")
            .defaultValue(true)
            .build();

    private final BooleanSetting gamemode = BooleanSetting.builder()
            .id("nametags_gamemode")
            .displayName("Gamemode")
            .description("Show player's gamemode")
            .defaultValue(false)
            .build();

    private final BooleanSetting ping = BooleanSetting.builder()
            .id("nametags_ping")
            .displayName("Ping")
            .description("Show player's ping")
            .defaultValue(false)
            .build();

    private final BooleanSetting health = BooleanSetting.builder()
            .id("nametags_health")
            .displayName("Health")
            .description("Show player's health")
            .defaultValue(true)
            .build();

    private final BooleanSetting distance = BooleanSetting.builder()
            .id("nametags_distance")
            .displayName("Distance")
            .description("Show distance to player")
            .defaultValue(true)
            .build();

    private final BooleanSetting pops = BooleanSetting.builder()
            .id("nametags_totem_pops")
            .displayName("Totem Pops")
            .description("Show player's totem pops")
            .defaultValue(true)
            .build();

    // Color Settings
    private final ColorSetting outline = ColorSetting.builder()
            .id("nametags_outline")
            .displayName("Outline")
            .description("Color for nametag outline")
            .defaultValue(new Color(255, 255, 255, 153))
            .build();

    private final BooleanSetting outlineEnabled = BooleanSetting.builder()
            .id("nametags_outline_enabled")
            .displayName("Outline Enabled")
            .description("Enable nametag outline")
            .defaultValue(true)
            .build();

    private final ColorSetting rect = ColorSetting.builder()
            .id("nametags_rect")
            .displayName("Rectangle")
            .description("Color for nametag background")
            .defaultValue(new Color(0, 0, 1, 153))
            .build();

    private final BooleanSetting rectEnabled = BooleanSetting.builder()
            .id("nametags_rect_enabled")
            .displayName("Rectangle Enabled")
            .description("Enable nametag background")
            .defaultValue(true)
            .build();

    // Armor Settings
    private final FloatSetting armorHeight = FloatSetting.builder()
            .id("nametags_armor_height")
            .displayName("Armor Height")
            .description("Vertical position of armor display")
            .defaultValue(0f)
            .minValue(-10f)
            .maxValue(10f)
            .step(1f)
            .build();

    private final EnumSetting<Armor> armorMode = EnumSetting.<Armor>builder()
            .id("nametags_armor_mode")
            .displayName("Armor Mode")
            .description("How armor is displayed on nametags")
            .defaultValue(Armor.All)
            .build();

    public Nametags() {
    	super("Nametags");

        this.setCategory(Category.of("Render"));
        this.setDescription("Renders nametags above relevant entities");

        this.addSetting(mode);
        this.addSetting(scale);

            // Create page group for organizing settings
            PageGroup settingsPages = PageGroup.Builder.builder()
                    .id("nametags_pages")
                    .displayName("Settings Pages")
                    .description("Nametag configuration options")
                    .build();

            // Create pages for original and new settings
            PageGroup.Page advancedPage = new PageGroup.Page("Payload");
            PageGroup.Page originalPage = new PageGroup.Page("Minecraft");

            // Add original settings to the original page
            originalPage.addSetting(frontcolor);
            originalPage.addSetting(unlimitedRange);
            originalPage.addSetting(forceShowMobNametags);
            originalPage.addSetting(forceShowPlayerNametags);
            originalPage.addSetting(showUnnamedMobs);
            originalPage.addSetting(showOtherEntities);
            originalPage.addSetting(showHP);
            originalPage.addSetting(drawBackground);

            // Set up setting groups for advanced page
            advancedPage.addSetting(generalSettings);
            advancedPage.addSetting(displaySettings);
            advancedPage.addSetting(colorSettings);

            // Add settings to general group
            generalSettings.addSetting(minScale);
            generalSettings.addSetting(scaled);
            generalSettings.addSetting(height);

            // Add settings to display group
            displaySettings.addSetting(armorMode);
            displaySettings.addSetting(armorHeight);
            displaySettings.addSetting(god);
            displaySettings.addSetting(gamemode);
            displaySettings.addSetting(ping);
            displaySettings.addSetting(health);
            displaySettings.addSetting(distance);
            displaySettings.addSetting(pops);

            // Add settings to color group
            colorSettings.addSetting(outline);
            colorSettings.addSetting(outlineEnabled);
            colorSettings.addSetting(rect);
            colorSettings.addSetting(rectEnabled);

            // Add pages to page group
            settingsPages.addPage(advancedPage);
            settingsPages.addPage(originalPage);

            // Add the page group to the module
            this.addSetting(settingsPages);

            // Register all settings and groups with SettingManager
            registerAllSettings();
        }

    public enum Armor {
        None, All, OnlyItems, OnlyArmor
    }

    public enum Mode {
        Minecraft, Payload
    }

    private void registerAllSettings() {
        // Register original settings
        SettingManager.registerSetting(frontcolor);
        SettingManager.registerSetting(scale);
        SettingManager.registerSetting(unlimitedRange);
        SettingManager.registerSetting(forceShowMobNametags);
        SettingManager.registerSetting(forceShowPlayerNametags);
        SettingManager.registerSetting(showUnnamedMobs);
        SettingManager.registerSetting(showOtherEntities);
        SettingManager.registerSetting(showHP);
        SettingManager.registerSetting(drawBackground);

        // Register setting groups
        SettingManager.registerSetting(generalSettings);
        SettingManager.registerSetting(displaySettings);
        SettingManager.registerSetting(colorSettings);

        // Register general settings
        SettingManager.registerSetting(minScale);
        SettingManager.registerSetting(scaled);
        SettingManager.registerSetting(height);

        // Register display settings
        SettingManager.registerSetting(god);
        SettingManager.registerSetting(gamemode);
        SettingManager.registerSetting(ping);
        SettingManager.registerSetting(health);
        SettingManager.registerSetting(distance);
        SettingManager.registerSetting(pops);

        // Register color settings
        SettingManager.registerSetting(outline);
        SettingManager.registerSetting(outlineEnabled);
        SettingManager.registerSetting(rect);
        SettingManager.registerSetting(rectEnabled);

        // Register armor settings
        SettingManager.registerSetting(armorHeight);
        SettingManager.registerSetting(armorMode);
    }

    private final HashMap<Entity, Text> originalNames = new HashMap<>();

    @Override
    public void onDisable() {
            for (Entity entity : new ArrayList<>(originalNames.keySet())) {
                if (entity != null && entity.isAlive()) {
                    Text originalName = originalNames.get(entity);
                    entity.setCustomName(originalName);
                    entity.setCustomNameVisible(originalName != null);
                }
            }
        originalNames.clear();
        Payload.getInstance().eventManager.RemoveListener(TickListener.class, this);
        Payload.getInstance().eventManager.RemoveListener(Render2DListener.class, this);
    }

    @Override
    public void onEnable() {
        Payload.getInstance().eventManager.AddListener(TickListener.class, this);
        Payload.getInstance().eventManager.AddListener(Render2DListener.class, this);
    }

    @Override
    public void onToggle() {
    }

    @Override
    public void onTick(TickEvent.Pre event) {
        if (nullCheck()) return;
        if (mode.getValue() == Mode.Payload) return;

        if (showUnnamedMobs.getValue()) {
            for (Entity entity : MC.world.getEntities()) {
                if (!(entity instanceof LivingEntity) || entity instanceof PlayerEntity) continue;

                if (entity.getCustomName() == null) {
                    originalNames.putIfAbsent(entity, entity.getCustomName());
                    entity.setCustomName(entity.getDisplayName());
                    entity.setCustomNameVisible(true);
                }
            }
        } else {
            for (Entity entity : new ArrayList<>(originalNames.keySet())) {
                if (entity != null && entity.isAlive()) {
                    Text originalName = originalNames.get(entity);
                    entity.setCustomName(originalName);
                    entity.setCustomNameVisible(originalName != null);
                }
            }
            originalNames.clear();
        }
    }

    @Override
    public void onTick(TickEvent.Post event) {

    }

    @Override
    public void onRender(Render2DEvent event) {
        if (mode.getValue() == Mode.Minecraft || MC.currentScreen != null) return;

        DrawContext context = event.getDrawContext();

        for (PlayerEntity ent : MC.world.getPlayers()) {
            if (ent == MC.player && MC.options.getPerspective().isFirstPerson()
                    && !Payload.getInstance().moduleManager.freecam.state.getValue()) continue;

            // Interpolate position
            double x = ent.prevX + (ent.getX() - ent.prevX) * event.getRenderTickCounter().getTickDelta(false);
            double y = ent.prevY + (ent.getY() - ent.prevY) * event.getRenderTickCounter().getTickDelta(false);
            double z = ent.prevZ + (ent.getZ() - ent.prevZ) * event.getRenderTickCounter().getTickDelta(false);

            Vec3d worldPos = new Vec3d(x, y + ent.getHeight() + height.getValue(), z);
            Vec3d screenPos = TextUtils.worldSpaceToScreenSpace(worldPos);

            if (screenPos == null || screenPos.z <= 0 || screenPos.z >= 1) continue;

            // Build nametag string
            String tag = "";
            if (god.getValue() && ent.hasStatusEffect(StatusEffects.SLOWNESS)) tag += "§4GOD ";
            if (ping.getValue()) tag += getEntityPing(ent) + "ms ";
            if (gamemode.getValue()) tag += translateGamemode(getEntityGamemode(ent)) + " ";
            tag += Formatting.RESET + ent.getName().getString();
            if (health.getValue()) tag += " " + getHealthColor(ent) + round2(ent.getAbsorptionAmount() + ent.getHealth());
            if (distance.getValue()) tag += " " + Formatting.RESET + String.format("%.1f", MC.player.distanceTo(ent)) + "m";
            if (pops.getValue()) {
                int popCount = Payload.getInstance().combatManager.getPop(ent.getName().getString());
                if (popCount > 0) tag += " §bPop " + Formatting.LIGHT_PURPLE + popCount;
            }

            float textWidth = MC.textRenderer.getWidth(tag);
            float textHeight = MC.textRenderer.fontHeight;

            float xPos = (float) screenPos.x - textWidth / 2f;
            float yPos = (float) screenPos.y;

            // Begin transform
            context.getMatrices().push();

            // Apply distance-based scaling
            float dist = (float) Math.cbrt(MC.cameraEntity.squaredDistanceTo(worldPos));
            float scaleVal = Math.max(scale.getValue() * (1 - dist * 0.01f * scaled.getValue()), minScale.getValue());
            context.getMatrices().translate(xPos + textWidth / 2f, yPos, 0);
            context.getMatrices().scale(scaleVal, scaleVal, 1);
            context.getMatrices().translate(-textWidth / 2f, 0, 0); // Centered alignment

            // Optional background rectangle
            if (rectEnabled.getValue()) {
                context.fill(-2, -2, (int) textWidth + 2, (int) textHeight + 2, rect.getValue().getColorAsInt());

                //Render2D.drawBox(context.getMatrices().peek().getPositionMatrix(), -2, -2, textWidth + 4, textHeight + 4, rect.getValue());
                //FUCK YOU FUCKING RENDER2D HAHAHAHA I FIXED THE RENDERING BUG DONT SE IT
            }

            // Optional outline
            if (outlineEnabled.getValue()) {

                context.drawBorder(-2, -2, (int) textWidth + 4, (int) textHeight + 4, outline.getValue().getColorAsInt());
                /*
                Render2D.drawBox(context.getMatrices().peek().getPositionMatrix(),
                        -3, -3, textWidth + 6, 1, outline.getValue()); // top
                Render2D.drawBox(context.getMatrices().peek().getPositionMatrix(),
                        -3, textHeight + 2, textWidth + 6, 1, outline.getValue()); // bottom
                Render2D.drawBox(context.getMatrices().peek().getPositionMatrix(),
                        -3, -3, 1, textHeight + 6, outline.getValue()); // left
                Render2D.drawBox(context.getMatrices().peek().getPositionMatrix(),
                        textWidth + 2, -3, 1, textHeight + 6, outline.getValue()); // right
                 */
            }


            // Draw the name tag
            context.drawText(
                    MC.textRenderer,
                    tag,
                    0,
                    0,
                    Payload.getInstance().friendsList.contains(ent)
                            ? java.awt.Color.GREEN.getRGB()
                            : java.awt.Color.WHITE.getRGB(),
                    true
            );

            if (armorMode.getValue() != Armor.None) {

                // Item rendering start
                List<ItemStack> stacks = new ArrayList<>();

                if (armorMode.getValue() != Armor.OnlyArmor) {
                    // Offhand first for symmetry
                    ItemStack offhand = ent.getOffHandStack();
                    if (!offhand.isEmpty()) stacks.add(offhand);
                }


                if (armorMode.getValue() != Armor.OnlyItems) {
                    // Armor (head to boots)
                    for (ItemStack armorStack : ent.getArmorItems()) {
                        if (!armorStack.isEmpty()) stacks.add(armorStack);
                    }
                }

                if (armorMode.getValue() != Armor.OnlyArmor) {
                    // Mainhand last
                    ItemStack mainhand = ent.getMainHandStack();
                    if (!mainhand.isEmpty()) stacks.add(mainhand);
                }

                float spacing = 2.0f;
                float iconSize = 16.0f;
                float totalWidth = stacks.size() * (iconSize + spacing) - spacing;

                float startX = (textWidth / 2f) - (totalWidth / 2f);
                float iconY = textHeight - 29.0f; // Padding above text

                for (int i = 0; i < stacks.size(); i++) {
                    ItemStack stack = stacks.get(i);
                    float iconX = startX + i * (iconSize + spacing);

                    context.getMatrices().push();

                    context.getMatrices().translate(iconX, iconY, 0);
                    context.getMatrices().scale(1.0f, 1.0f, 1.0f);

                    context.drawItem(stack, 0, Math.round(armorHeight.getValue()));
                    //context.drawItemInSlot(MC.textRenderer, stack, 0, 0);

                    context.getMatrices().pop();
                }
            }

            context.getMatrices().pop();
        }
    }


    public static String getEntityPing(PlayerEntity entity) {
        if (MC.getNetworkHandler() == null) return "-1";
        PlayerListEntry playerListEntry = MC.getNetworkHandler().getPlayerListEntry(entity.getUuid());
        if (playerListEntry == null) return "-1";
        int ping = playerListEntry.getLatency();
        Formatting color = Formatting.GREEN;
        if (ping >= 100) {
            color = Formatting.YELLOW;
        }
        if (ping >= 250) {
            color = Formatting.RED;
        }
        return color.toString() + ping;
    }

    public static GameMode getEntityGamemode(PlayerEntity entity) {
        if (entity == null) return null;
        PlayerListEntry playerListEntry = MC.getNetworkHandler().getPlayerListEntry(entity.getUuid());
        return playerListEntry == null ? null : playerListEntry.getGameMode();
    }

    private String translateGamemode(GameMode gamemode) {
        if (gamemode == null) return "§7[BOT]";
        return switch (gamemode) {
            case SURVIVAL -> "§b[S]";
            case CREATIVE -> "§c[C]";
            case SPECTATOR -> "§7[SP]";
            case ADVENTURE -> "§e[A]";
        };
    }

    private Formatting getHealthColor(@NotNull PlayerEntity entity) {
        int health = (int) ((int) entity.getHealth() + entity.getAbsorptionAmount());
        if (health >= 18) {
            return Formatting.GREEN;
        }
        if (health >= 12) {
            return Formatting.YELLOW;
        }
        if (health >= 6) {
            return Formatting.RED;
        }
        return Formatting.DARK_RED;
    }

    public static float round2(double value) {
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(1, RoundingMode.HALF_UP);
        return bd.floatValue();
    }

    public boolean getBackdrop() {
        return drawBackground.get();
    }

    public Color getColorBank() {
        return frontcolor.get();
    }

    public float getNametagScale() {
        return this.scale.getValue();
    }

    public boolean isUnlimitedRange()
    {
        return this.unlimitedRange.getValue();
    }

    public boolean shouldForceMobNametags()
    {
        return this.forceShowMobNametags.getValue();
    }

    public boolean shouldForcePlayerNametags()
    {
        return this.forceShowPlayerNametags.getValue();
    }

    public boolean showHealth()
    {
        return this.showHP.getValue();
    }

    public Text addHealth(LivingEntity entity, Text nametag)
    {
        if(!this.state.getValue())
            return nametag;

        int health = (int)entity.getHealth();

        MutableText formattedHealth = Text.literal(" ").append(Integer.toString(health)).formatted(getColor(health));
        return ((MutableText)nametag).append(formattedHealth);
    }

    private Formatting getColor(int health)
    {
        if(health <= 5)
            return Formatting.DARK_RED;

        if(health <= 10)
            return Formatting.GOLD;

        if(health <= 15)
            return Formatting.YELLOW;

        return Formatting.GREEN;
    }

    public boolean isMinecraft() {
        return mode.getValue() == Mode.Minecraft;
    }
}