package net.payload.gui.navigation.huds;

import net.payload.gui.Rectangle;
import net.payload.gui.SheepHudSize;
import net.payload.gui.colors.Color;
import net.payload.gui.navigation.HudWindow;
import net.payload.settings.SettingManager;
import net.payload.utils.render.Render2D;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

import static net.payload.gui.GuiManager.sheepSizeMode;

public class Sheep extends HudWindow {
    private static final int FRAME_DELAY = 16;
    private static final int FRAME_COUNT = 12;
    private static final String MOD_ID = "payload";
    private static final float BASE_SIZE = 64f;
    private static final Color DEFAULT_COLOR = new Color(255, 255, 255, 255);

    private float currentScale = 1.0f;
    private final MinecraftClient client;
    private final List<Identifier> frames;
    private int currentFrame;
    private long lastFrameTime;

    public Sheep(int x, int y) {
        super("PrideSheep", x, y);
        this.client = MinecraftClient.getInstance();
        this.frames = new ArrayList<>(FRAME_COUNT);
        this.currentFrame = 0;
        this.lastFrameTime = System.currentTimeMillis();

        SettingManager.registerSetting(sheepSizeMode);

        loadFrames();
        updateSize();
    }

    private void updateSize() {
        switch (sheepSizeMode.getValue()) {
            case SMALL -> currentScale = 1.0f;
            case LARGE -> currentScale = 3.0f;
            case HUGE -> currentScale = 5.0f;
            default -> currentScale = 2.0f;
        }

        float scaledSize = BASE_SIZE * currentScale;

        position.setValue(new Rectangle(
                position.getValue().getX(),
                position.getValue().getY(),
                scaledSize,
                scaledSize
        ));

        this.setWidth(scaledSize);
        this.setHeight(scaledSize);
        this.minWidth = scaledSize;
        this.maxWidth = scaledSize;
        this.minHeight = scaledSize;
        this.maxHeight = scaledSize;
    }

    private void loadFrames() {
        for (int i = 1; i <= FRAME_COUNT; i++) {
            try {
                Identifier frameId = Identifier.of(MOD_ID, String.format("textures/frames/frame%d.png", i));
                frames.add(frameId);
            } catch (Exception e) {
                System.err.printf("Failed to load frame %d: %s%n", i, e.getMessage());
            }
        }
    }

    @Override
    public void update() {
        super.update();
        updateSize();
    }

    @Override
    public void draw(DrawContext drawContext, float partialTicks) {
        if (!isVisible() || frames.isEmpty()) {
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
            matrixStack.scale(currentScale, currentScale, currentScale);

            float scaledX = pos.getX() / currentScale;
            float scaledY = pos.getY() / currentScale;

            updateFrame();

            Identifier currentTexture = frames.get(currentFrame);
            Matrix4f matrix = new Matrix4f(matrixStack.peek().getPositionMatrix());

            Render2D.drawTexturedQuad(
                    matrix,
                    currentTexture,
                    scaledX,
                    scaledY,
                    BASE_SIZE,
                    BASE_SIZE,
                    DEFAULT_COLOR
            );
        } finally {
            matrixStack.pop();
        }

        super.draw(drawContext, partialTicks);
    }

    private void updateFrame() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastFrameTime >= FRAME_DELAY) {
            currentFrame = (currentFrame + 1) % frames.size();
            lastFrameTime = currentTime;
        }
    }
}