package net.payload.gui.navigation.huds;

import net.payload.gui.GuiManager;
import net.payload.gui.Rectangle;
import net.payload.gui.ResizeMode;
import net.payload.gui.navigation.HudWindow;
import net.payload.settings.SettingManager;
import net.payload.utils.render.Render2D;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.ChunkStatus;

public class ShulkerCountHud extends HudWindow {
    private static final MinecraftClient MC = MinecraftClient.getInstance();
    private static final float BASE_HEIGHT = 20f;
    private String labelText;
    private String countText;
    private float currentScale = 1.0f;

    public ShulkerCountHud(int x, int y) {
        super("ShulkerCount", x, y, 50, 24);
        resizeMode = ResizeMode.None;
        SettingManager.registerSetting(GuiManager.shulkSize);
        SettingManager.registerSetting(GuiManager.shulkLabelColor);
        SettingManager.registerSetting(GuiManager.shulkCountColor);

        updateDimensions();
    }

    private float calculateWidth() {
        if (MC.player == null || MC.textRenderer == null || labelText == null || countText == null) {
            return 50f;
        }

        float labelWidth = MC.textRenderer.getWidth(labelText);
        float countWidth = MC.textRenderer.getWidth(countText);
        float bracketsWidth = MC.textRenderer.getWidth("[]");
        return labelWidth + countWidth + bracketsWidth + 8f;
    }

    private void updateDimensions() {
        float baseWidth = calculateWidth();

        switch (GuiManager.shulkSize.getValue()) {
            case SMALL -> currentScale = 0.5f;
            case LARGE -> currentScale = 2.0f;
            case HUGE -> currentScale = 3.0f;
            default -> currentScale = 1.0f;
        }

        float finalWidth = baseWidth * currentScale * 1.87f;
        float finalHeight = BASE_HEIGHT * currentScale;

        Rectangle currentPos = position.getValue();
        position.setValue(new Rectangle(
                currentPos.getX(),
                currentPos.getY(),
                finalWidth,
                finalHeight
        ));

        this.setWidth(finalWidth);
        this.setHeight(finalHeight);
        this.minWidth = this.maxWidth = finalWidth;
        this.minHeight = this.maxHeight = finalHeight;
    }

    @Override
    public void draw(DrawContext drawContext, float partialTicks) {
        if (!isVisible() || MC.world == null || MC.player == null) {
            super.draw(drawContext, partialTicks);
            return;
        }

        Rectangle pos = position.getValue();
        if (!pos.isDrawable()) {
            super.draw(drawContext, partialTicks);
            return;
        }

        MatrixStack matrixStack = drawContext.getMatrices();
        matrixStack.push();
        try {
            int shulkerCount = countShulkers();

            switch (GuiManager.shulkCountTextStyle.getValue()) {
                case SHORT -> labelText = "Shulks: ";
                case NORMAL -> labelText = "Shulkers: ";
                case LONG -> labelText = "Shulker Boxes: ";
                default -> labelText = "Shulkers: ";
            }
            countText = String.valueOf(shulkerCount);

            updateDimensions();

            matrixStack.scale(currentScale, currentScale, currentScale);

            float baseX = pos.getX() / currentScale;
            float baseY = (pos.getY() / currentScale) + 3;

            // Draw label
            Render2D.drawString(
                    drawContext,
                    labelText,
                    baseX,
                    baseY,
                    GuiManager.shulkLabelColor.getValue().getColorAsInt()
            );

            // Draw opening bracket
            float bracketX = baseX + MC.textRenderer.getWidth(labelText) * 2;
            Render2D.drawString(
                    drawContext,
                    "[",
                    bracketX,
                    baseY,
                    GuiManager.shulkLabelColor.getValue().getColorAsInt()
            );

            // Draw count
            float countX = bracketX + MC.textRenderer.getWidth("[ ");
            Render2D.drawString(
                    drawContext,
                    countText,
                    countX,
                    baseY,
                    GuiManager.shulkCountColor.getValue().getColorAsInt()
            );

            // Calculate and draw closing bracket
            float baseSpacing = MC.textRenderer.getWidth("  ");
            float bracketSpacing;
            int digits = countText.length();

            switch (digits) {
                case 1 -> bracketSpacing = baseSpacing * 1.25f;
                case 2 -> bracketSpacing = baseSpacing * 2.02f;
                case 3 -> bracketSpacing = baseSpacing * 2.75f;
                default -> bracketSpacing = baseSpacing * 3.2f;
            }

            Render2D.drawString(
                    drawContext,
                    "]",
                    bracketX + MC.textRenderer.getWidth("[") +
                            MC.textRenderer.getWidth(countText) +
                            bracketSpacing,
                    baseY,
                    GuiManager.shulkLabelColor.getValue().getColorAsInt()
            );
        } finally {
            matrixStack.pop();
        }

        super.draw(drawContext, partialTicks);
    }

    private int countShulkers() {
        int shulkerCount = 0;
        ChunkPos playerChunk = MC.player.getChunkPos();
        int viewDistance = MC.options.getViewDistance().getValue();

        for (int x = playerChunk.x - viewDistance; x <= playerChunk.x + viewDistance; x++) {
            for (int z = playerChunk.z - viewDistance; z <= playerChunk.z + viewDistance; z++) {
                WorldChunk chunk = (WorldChunk) MC.world.getChunk(x, z, ChunkStatus.FULL, false);
                if (chunk != null && !chunk.isEmpty()) {
                    for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                        if (blockEntity instanceof ShulkerBoxBlockEntity) {
                            shulkerCount++;
                        }
                    }
                }
            }
        }

        return shulkerCount;
    }
}