package net.payload.gui.navigation.huds;

import net.payload.gui.GuiManager;
import net.payload.gui.Rectangle;
import net.payload.gui.ResizeMode;
import net.payload.gui.navigation.HudWindow;
import net.payload.utils.render.Render2D;
import net.payload.settings.SettingManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.ChunkStatus;

public class ChestCountHud extends HudWindow {

    private static final MinecraftClient MC = MinecraftClient.getInstance();
    private static final float BASE_HEIGHT = 20f;
    private String labelText;
    private String countText;
    private float currentScale = 1.0f;

    public ChestCountHud(int x, int y) {
        super("DubCount", x, y, 50, 24);
        resizeMode = ResizeMode.None;
        SettingManager.registerSetting(GuiManager.dubSize);
        SettingManager.registerSetting(GuiManager.dubLabelColor);
        SettingManager.registerSetting(GuiManager.dubCountColor);

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

        switch (GuiManager.dubSize.getValue()) {
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
        if (isVisible() && MC.world != null && MC.player != null) {
            Rectangle pos = position.getValue();
            if (pos.isDrawable()) {
                int chestCount = 0;

                ChunkPos playerChunk = MC.player.getChunkPos();
                int viewDistance = MC.options.getViewDistance().getValue();

                for (int x = playerChunk.x - viewDistance; x <= playerChunk.x + viewDistance; x++) {
                    for (int z = playerChunk.z - viewDistance; z <= playerChunk.z + viewDistance; z++) {
                        WorldChunk chunk = (WorldChunk) MC.world.getChunk(x, z, ChunkStatus.FULL, false);
                        if (chunk != null && !chunk.isEmpty()) {
                            for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                                if (blockEntity instanceof ChestBlockEntity) {
                                    chestCount++;
                                }
                            }
                        }
                    }
                }

                float doubleChestCount = chestCount / 2.0f;

                switch (GuiManager.dubCountTextStyle.getValue()) {
                    case SHORT -> labelText = "Dubs: ";
                    case NORMAL -> labelText = "Chests: ";
                    case LONG -> labelText = "Double Chests: ";
                    default -> labelText = "Dubs: ";
                }
                countText = String.format("%.1f", doubleChestCount);

                updateDimensions();

                MatrixStack matrixStack = drawContext.getMatrices();
                matrixStack.push();
                matrixStack.scale(currentScale, currentScale, currentScale);

                float baseX = pos.getX() / currentScale;
                float baseY = (pos.getY() / currentScale) + 3;

                Render2D.drawString(
                        drawContext,
                        labelText,
                        baseX,
                        baseY,
                        GuiManager.dubLabelColor.getValue().getColorAsInt()
                );

                Render2D.drawString(
                        drawContext,
                        "[",
                        baseX + MC.textRenderer.getWidth(labelText) * 2,
                        baseY,
                        GuiManager.dubLabelColor.getValue().getColorAsInt()
                );

                Render2D.drawString(
                        drawContext,
                        countText,
                        baseX + MC.textRenderer.getWidth(labelText) * 2 + MC.textRenderer.getWidth("[ "),
                        baseY,
                        GuiManager.dubCountColor.getValue().getColorAsInt()
                );

                float bracketSpacing;
                float baseSpacing = MC.textRenderer.getWidth("    ");

                int digitsBeforeDecimal = countText.contains(".") ?
                        countText.indexOf('.') : countText.length();

                switch (digitsBeforeDecimal) {
                    case 1: // X.X
                        bracketSpacing = baseSpacing *1.25f;
                        break;
                    case 2: // XX.X
                        bracketSpacing = baseSpacing * 1.5f;
                        break;
                    case 3: // XXX.X
                        bracketSpacing = baseSpacing * 2f;
                        break;
                    default: // XXXX.X
                        bracketSpacing = baseSpacing * 2.5f;
                        break;
                }

                Render2D.drawString(
                        drawContext,
                        "]",
                        baseX + MC.textRenderer.getWidth(labelText) * 2 +
                                MC.textRenderer.getWidth("[") +
                                MC.textRenderer.getWidth(countText) +
                                bracketSpacing,
                        baseY,
                        GuiManager.dubLabelColor.getValue().getColorAsInt()
                );

                matrixStack.pop();
            }
        }

        super.draw(drawContext, partialTicks);
    }
}