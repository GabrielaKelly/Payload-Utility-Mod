package net.payload.gui.navigation.huds;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.payload.Payload;
import net.payload.event.events.ReceivePacketEvent;
import net.payload.event.listeners.ReceivePacketListener;
import net.payload.gui.GuiManager;
import net.payload.gui.Rectangle;
import net.payload.gui.ResizeMode;
import net.payload.gui.navigation.HudWindow;
import net.payload.settings.SettingManager;
import net.payload.utils.render.Render2D;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static net.payload.gui.GuiManager.*;

public class IncomingPacketHud extends HudWindow implements ReceivePacketListener {
    private static final int BASE_WIDTH = 100;
    private static final int BASE_HEIGHT = 50;
    private static final int GRAPH_POINTS = 100;
    private static final float GRAPH_ALPHA = 0.7f;

    private float currentScale = 1.0f;
    private final AtomicInteger packetsThisSecond = new AtomicInteger(0);

    private record PacketDataPoint(long timestamp, int packetCount) {}
    private final List<PacketDataPoint> packetHistory;
    private String packetText = null;
    private int currentPacketCount = 0;
    private int tickCounter = 0;
    private long lastUpdateTime;

    public IncomingPacketHud(int x, int y) {
        super("IncomingPacketHUD", x, y, BASE_WIDTH, BASE_HEIGHT);
        resizeMode = ResizeMode.None;

        packetHistory = new ArrayList<>();
        lastUpdateTime = System.currentTimeMillis();

        SettingManager.registerSetting(incpacketsizeMode);
        SettingManager.registerSetting(GuiManager.PacketUpdateDelay);
        SettingManager.registerSetting(GuiManager.packetShowText);
        SettingManager.registerSetting(GuiManager.packetBackgroundColor);
        SettingManager.registerSetting(GuiManager.packethudcolor);

        Payload.getInstance().eventManager.AddListener(ReceivePacketListener.class, this);

        updateSize();
    }

    private void updateSize() {
        switch (incpacketsizeMode.getValue()) {
            case SMALL -> currentScale = 1.0f;
            case LARGE -> currentScale = 3.0f;
            case MASSIVE -> currentScale = 4.0f;
            case HUGE -> currentScale = 5.0f;
            default -> currentScale = 2.0f;
        }

        float scaledWidth = BASE_WIDTH * currentScale;
        float scaledHeight = BASE_HEIGHT * currentScale;

        Rectangle currentPos = position.getValue();
        position.setValue(new Rectangle(
                currentPos.getX(),
                currentPos.getY(),
                scaledWidth,
                scaledHeight
        ));

        this.setWidth(scaledWidth);
        this.setHeight(scaledHeight);
        this.minWidth = scaledWidth;
        this.maxWidth = scaledWidth;
        this.minHeight = scaledHeight;
        this.maxHeight = scaledHeight;
    }

    @Override
    public void onReceivePacket(ReceivePacketEvent readPacketEvent) {
        packetsThisSecond.incrementAndGet();
    }

    private void updatePacketCount() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastUpdateTime >= 1000) {
            currentPacketCount = packetsThisSecond.getAndSet(0);
            packetHistory.add(new PacketDataPoint(currentTime, currentPacketCount));

            while (packetHistory.size() > GRAPH_POINTS) {
                packetHistory.remove(0);
            }

            packetText = String.format("Incoming Packets: %d/s", currentPacketCount);
            lastUpdateTime = currentTime;
        }
    }

    private void drawPacketGraph(DrawContext drawContext, Rectangle pos) {
        if (packetHistory.isEmpty()) return;

        MatrixStack matrixStack = drawContext.getMatrices();
        matrixStack.push();
        try {
            float graphX = pos.getX();
            float graphY = pos.getY();
            float scaledWidth = BASE_WIDTH * currentScale;
            float scaledHeight = BASE_HEIGHT * currentScale;

            // Draw background
            Render2D.drawBox(
                    matrixStack.peek().getPositionMatrix(),
                    graphX,
                    graphY,
                    scaledWidth,
                    scaledHeight,
                    GuiManager.packetBackgroundColor.getValue()
            );

            // Calculate max packets for scaling
            int maxPackets = packetHistory.stream()
                    .mapToInt(PacketDataPoint::packetCount)
                    .max()
                    .orElse(1);

            int points = Math.min(GRAPH_POINTS, packetHistory.size());
            float pointWidth = scaledWidth / (points - 1);

            // Draw graph lines
            for (int i = 0; i < points - 1; i++) {
                PacketDataPoint current = packetHistory.get(packetHistory.size() - points + i);
                PacketDataPoint next = packetHistory.get(packetHistory.size() - points + i + 1);

                float x1 = graphX + (i * pointWidth);
                float x2 = graphX + ((i + 1) * pointWidth);

                float y1 = graphY + scaledHeight - ((current.packetCount() / (float)maxPackets) * scaledHeight);
                float y2 = graphY + scaledHeight - ((next.packetCount() / (float)maxPackets) * scaledHeight);

                Render2D.drawLine(
                        matrixStack.peek().getPositionMatrix(),
                        x1,
                        y1,
                        x2,
                        y2,
                        GuiManager.packethudcolor.getValue()
                );
            }
        } finally {
            matrixStack.pop();
        }
    }

    @Override
    public void update() {
        super.update();
        updateSize();

        float updateInterval = Math.max(1, (int)(GuiManager.PacketUpdateDelay.getValue() * 20));
        if (++tickCounter >= updateInterval) {
            tickCounter = 0;
            updatePacketCount();
        }
    }

    @Override
    public void draw(DrawContext drawContext, float partialTicks) {
        if (!isVisible()) {
            super.draw(drawContext, partialTicks);
            return;
        }

        Rectangle pos = position.getValue();
        if (pos.isDrawable()) {
            drawPacketGraph(drawContext, pos);

            if (GuiManager.packetShowText.getValue() && packetText != null) {
                MatrixStack matrixStack = drawContext.getMatrices();
                matrixStack.push();
                try {
                    matrixStack.scale(currentScale * 0.30f, currentScale * 0.30f, currentScale);

                    float textY = pos.getY() + (BASE_HEIGHT * currentScale) - (29 * currentScale);

                    Render2D.drawString(
                            drawContext,
                            packetText,
                            pos.getX() / (currentScale * 0.30f) + 6,
                            textY / (currentScale * 0.30f) - 65,
                            GuiManager.packethudcolor.getValue().getColorAsInt()
                    );
                } finally {
                    matrixStack.pop();
                }
            }
        }

        super.draw(drawContext, partialTicks);
    }

    public void cleanup() {
        Payload.getInstance().eventManager.RemoveListener(ReceivePacketListener.class, this);
    }

    public int getCurrentPacketCount() {
        return currentPacketCount;
    }
}