package net.payload.gui.navigation.huds;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.payload.Payload;
import net.payload.SoundGenerator;
import net.payload.event.events.MouseClickEvent;
import net.payload.event.listeners.MouseClickListener;
import net.payload.gui.Rectangle;
import net.payload.gui.ResizeMode;
import net.payload.gui.colors.Color;
import net.payload.gui.navigation.HudWindow;
import net.payload.utils.math.CacheTimer;
import net.payload.utils.player.PlayerUtils;
import net.payload.utils.render.Render2D;
import net.payload.utils.types.MouseAction;
import net.payload.utils.types.MouseButton;
import org.joml.Matrix4f;

import java.util.Random;

import static net.payload.utils.render.TextureBank.*;

public class HaloHud extends HudWindow implements MouseClickListener {
    private float logoWidth;
    private float logoHeight;
    private final MinecraftClient client;
    private boolean hurtTrigger = true;

    private final CacheTimer sprintDelay = new CacheTimer();

    private final Identifier[] halohuds = {halohud0, halohud1, halohud2, halohud3, halohud4, halohud5};

    public HaloHud() {
        super("Halo Hud", 0, 0, MinecraftClient.getInstance().getWindow().getScaledWidth(), MinecraftClient.getInstance().getWindow().getScaledHeight());
        this.client = MinecraftClient.getInstance();

        resizeMode = ResizeMode.None;
        minWidth = (float) MinecraftClient.getInstance().getWindow().getScaledWidth();
        minHeight = (float) MinecraftClient.getInstance().getWindow().getScaledHeight();
        maxWidth = (float) MinecraftClient.getInstance().getWindow().getScaledWidth();
        maxHeight = (float) MinecraftClient.getInstance().getWindow().getScaledHeight();
        Payload.getInstance().eventManager.AddListener(MouseClickListener.class, this);

        updateLogoSize();
    }

    @Override
    public void update() {
        super.update();
        updateLogoSize();
    }

    @Override
    public void draw(DrawContext drawContext, float partialTicks) {
        if (!isVisible()) return;

        Rectangle pos = position.getValue();
        if (!pos.isDrawable()) return;

        MatrixStack matrixStack = drawContext.getMatrices();
        matrixStack.push();
        try {
            // Handle sprint sound
            if (PlayerUtils.isMoving() && PlayerUtils.isSprinting() && sprintDelay.passed(5000)) {
                SoundGenerator.halosprint();
                sprintDelay.reset();
            }

            // Draw the HUD
            Matrix4f matrix = matrixStack.peek().getPositionMatrix();
            Render2D.drawTexturedQuad(
                    matrix,
                    CurrentHud(),
                    0,
                    0,
                    logoWidth,
                    logoHeight,
                    new Color(255, 255, 255, 255)
            );
        } finally {
            matrixStack.pop();
        }
    }

    public static int getRandomOneOrTwo() {
        Random random = new Random();
        return random.nextInt(2) + 1;
    }

    @Override
    public void onMouseClick(MouseClickEvent event) {
        if (isVisible() && MC.inGameHud != null) {
            if (event.button == MouseButton.LEFT && event.action == MouseAction.DOWN) {
                int result = getRandomOneOrTwo();
                SoundGenerator.haloshoot();
                if (result != 1) {
                    SoundGenerator.haloshoot2();
                }
            }
        }
    }

    private void updateLogoSize() {
        logoWidth = client.getWindow().getScaledWidth() * 3;
        logoHeight = client.getWindow().getScaledHeight() * 3;
    }

    private Identifier CurrentHud() {
        if (!MC.player.isAlive()) {
            return halohuds[5];
        }

        float health = MC.player.getHealth();

        if (health <= 3f) {
            if (hurtTrigger) {
                SoundGenerator.halohurt();
                hurtTrigger = false;
            }
            return halohuds[4]; // Very low health
        } else if (health <= 6f) {
            return halohuds[3]; // Low health
        } else if (health <= 11f) {
            return halohuds[2]; // Moderate health
        } else if (health <= 16f) {
            return halohuds[1]; // High health
        } else {
            if (!hurtTrigger) {
                hurtTrigger = true;
            }
            return halohuds[0]; // Full health
        }
    }
}