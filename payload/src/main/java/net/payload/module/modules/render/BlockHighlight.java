/**
 * Block Highlight Module
 * Highlights the block player is looking at
 */
package net.payload.module.modules.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.payload.Payload;
import net.payload.event.events.Render3DEvent;
import net.payload.event.listeners.Render3DListener;
import net.payload.gui.colors.Color;
import net.payload.module.Category;
import net.payload.module.Module;
import net.payload.settings.types.ColorSetting;
import net.payload.settings.types.FloatSetting;
import net.payload.utils.render.Render3D;

public class BlockHighlight extends Module implements Render3DListener {

    private final MinecraftClient mc = MinecraftClient.getInstance();

    private final ColorSetting blockColor = ColorSetting.builder()
            .id("block_highlight_color")
            .displayName("Block Color")
            .defaultValue(new Color(255, 0, 0, 30))
            .build();

    private final FloatSetting lineThickness = FloatSetting.builder()
            .id("block_highlight_thickness")
            .displayName("Line Thickness")
            .defaultValue(0f)
            .minValue(0.1f)
            .maxValue(5f)
            .step(0.1f)
            .build();

    public BlockHighlight() {
        super("BlockHighlight");
        this.setName("BlockHighlight");
        this.setCategory(Category.of("render"));
        this.setDescription("Highlights the block you're looking at");

        this.addSetting(blockColor);
        this.addSetting(lineThickness);
    }

    @Override
    public void onEnable() {
        Payload.getInstance().eventManager.AddListener(Render3DListener.class, this);
    }

    @Override
    public void onDisable() {
        Payload.getInstance().eventManager.RemoveListener(Render3DListener.class, this);
    }

    @Override
    public void onToggle() {
        // Not needed for this module
    }

    @Override
    public void onRender(Render3DEvent event) {
        if (mc.world == null || mc.player == null) return;

        // Get the block the player is looking at
        HitResult hitResult = mc.crosshairTarget;

        if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHit = (BlockHitResult) hitResult;
            BlockPos pos = blockHit.getBlockPos();

            // Create a box around the block
            Box box = new Box(pos);

            // Render the highlight
            Render3D.draw3DBox(
                    event.GetMatrix(), event.getCamera(),
                    box,
                    blockColor.getValue(),
                    lineThickness.getValue()
            );
        }
    }
}