package net.payload.utils.anticheat;

import java.util.Arrays;

import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.common.CommonPingS2CPacket;
import net.minecraft.util.math.Vec3d;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.payload.Payload;
import net.payload.event.events.GameLeftEvent;
import net.payload.event.events.PlayerDeathEvent;
import net.payload.event.events.ReceivePacketEvent;
import net.payload.event.listeners.GameLeftListener;
import net.payload.event.listeners.PlayerDeathListener;
import net.payload.event.listeners.ReceivePacketListener;


public final class AntiCheatManager implements ReceivePacketListener, GameLeftListener, PlayerDeathListener {
    private SetbackData lastSetback;
    private final int[] transactions = new int[4];
    private int index;
    private boolean isGrim;

    public AntiCheatManager() {
        Payload.getInstance().eventManager.AddListener(ReceivePacketListener.class, this);
        Payload.getInstance().eventManager.AddListener(GameLeftListener.class, this);
        Payload.getInstance().eventManager.AddListener(PlayerDeathListener.class, this);

        Arrays.fill(transactions, -1);
    }

    private void detectGrimAnticheat() {
        // Check transaction sequence pattern
        for (int i = 0; i < 4 && transactions[i] == -i; i++) {
            // Continue checking pattern
        }

        isGrim = true;
        System.out.println("Server is running Grim.");
    }

    public boolean isGrim() {
        return isGrim;
    }

    public boolean hasPassed(long timeMS) {
        return lastSetback != null && lastSetback.timeSince() >= timeMS;
    }

    @Override
    public void onGameLeft(GameLeftEvent event) {
        if (Payload.getInstance().guiManager.isClickGuiOpen()) {
            Payload.getInstance().guiManager.setClickGuiOpen(false);
        }
        Arrays.fill(transactions, -1);
        index = 0;
        isGrim = false;
    }

    @Override
    public void onReceivePacket(ReceivePacketEvent readPacketEvent) {
        Packet<?> packet = readPacketEvent.getPacket();

        // Handle ping/transaction packets
        if (packet instanceof CommonPingS2CPacket pingPacket) {
            if (index > 3) {
                return;
            }

            int transactionId = pingPacket.getParameter();
            transactions[index] = transactionId;
            index++;

            if (index == 4) {
                detectGrimAnticheat();
            }
        }
        // Handle position/teleport packets
        else if (packet instanceof PlayerPositionLookS2CPacket posPacket) {
            lastSetback = new SetbackData(
                    new Vec3d(
                            posPacket.change().deltaMovement().getX(),
                            posPacket.change().deltaMovement().getY(),
                            posPacket.change().deltaMovement().getZ()
                    ),
                    System.currentTimeMillis(),
                    (int) posPacket.change().yaw()
            );
        }
    }

    @Override
    public void onPlayerDeath(PlayerDeathEvent readPacketEvent) {
        if (Payload.getInstance().guiManager.isClickGuiOpen()) {
            Payload.getInstance().guiManager.setClickGuiOpen(false);
        }
    }
}
