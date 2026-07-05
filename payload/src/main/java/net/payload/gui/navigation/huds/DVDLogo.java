package net.payload.gui.navigation.huds;

import net.payload.gui.Rectangle;
import net.payload.gui.ResizeMode;
import net.payload.gui.navigation.HudWindow;
import net.payload.utils.render.Render2D;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.payload.gui.colors.Color;
import org.joml.Matrix4f;

import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

import static net.payload.utils.render.TextureBank.*;

public class DVDLogo extends HudWindow {
    private static final int LOGO_WIDTH = 94;
    private static final int LOGO_HEIGHT = 54;
    private static final float BASE_SPEED = 100.0f;
    private static final Color DEFAULT_COLOR = new Color(255, 255, 255, 255);

    private float logoX;
    private float logoY;
    private float xSpeed;
    private float ySpeed;
    private long lastTime;
    private final AtomicReference<Identifier> logoTexture;
    private final MinecraftClient client;
    private final Random random;
    private final Identifier[] dvdlogos = {dvd, dvd2, dvd3, dvd4, dvd5, dvd6};
    private Identifier lastTexture;
    private boolean needsTextureUpdate;

    public DVDLogo(int startX, int startY) {
        super("DVD Logo", startX, startY, LOGO_WIDTH, LOGO_HEIGHT);
        this.client = MinecraftClient.getInstance();
        this.random = new Random();
        this.logoTexture = new AtomicReference<>(randomdvd());
        this.lastTexture = logoTexture.get();
        this.logoX = startX + 10;
        this.logoY = startY + 10;
        this.xSpeed = BASE_SPEED;
        this.ySpeed = BASE_SPEED;
        this.lastTime = System.nanoTime();
        this.needsTextureUpdate = false;

        resizeMode = ResizeMode.None;
        minWidth = (float) LOGO_WIDTH;
        minHeight = (float) LOGO_HEIGHT;
        maxHeight = (float) LOGO_HEIGHT;
    }

    @Override
    public void update() {
        super.update();
        updatePosition();
    }

    @Override
    public void draw(DrawContext drawContext, float partialTicks) {
        if (!isVisible()) return;

        Rectangle pos = position.getValue();
        if (!pos.isDrawable()) return;

        MatrixStack matrixStack = drawContext.getMatrices();
        matrixStack.push();
        try {
            if (needsTextureUpdate) {
                logoTexture.set(randomdvd());
                needsTextureUpdate = false;
            }

            Matrix4f matrix = matrixStack.peek().getPositionMatrix();
            Render2D.drawTexturedQuad(
                    matrix,
                    logoTexture.get(),
                    logoX,
                    logoY,
                    LOGO_WIDTH,
                    LOGO_HEIGHT,
                    DEFAULT_COLOR
            );
        } finally {
            matrixStack.pop();
        }

        super.draw(drawContext, partialTicks);
    }

    private void updatePosition() {
        long currentTime = System.nanoTime();
        float deltaTime = (currentTime - lastTime) / 1_000_000_000.0f;
        lastTime = currentTime;

        // Update position with delta time
        float newX = logoX + xSpeed * deltaTime;
        float newY = logoY + ySpeed * deltaTime;

        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();

        // Check boundaries and handle collisions
        boolean collision = false;

        if (newX < 0) {
            newX = 0;
            xSpeed = -xSpeed;
            collision = true;
        } else if (newX > screenWidth - LOGO_WIDTH) {
            newX = screenWidth - LOGO_WIDTH;
            xSpeed = -xSpeed;
            collision = true;
        }

        if (newY < 0) {
            newY = 0;
            ySpeed = -ySpeed;
            collision = true;
        } else if (newY > screenHeight - LOGO_HEIGHT) {
            newY = screenHeight - LOGO_HEIGHT;
            ySpeed = -ySpeed;
            collision = true;
        }

        // Update position
        logoX = newX;
        logoY = newY;

        if (collision) {
            needsTextureUpdate = true;
        }
    }

    private Identifier randomdvd() {
        Identifier newImage;
        int attempts = 0;
        int maxAttempts = 10;

        do {
            int randomIndex = random.nextInt(dvdlogos.length);
            newImage = dvdlogos[randomIndex];
            attempts++;
        } while (newImage.equals(lastTexture) && attempts < maxAttempts);

        lastTexture = newImage;
        return newImage;
    }

    public void reset() {
        logoX = position.getValue().getX();
        logoY = position.getValue().getY();
        xSpeed = BASE_SPEED;
        ySpeed = BASE_SPEED;
        lastTime = System.nanoTime();
        needsTextureUpdate = true;
    }
}