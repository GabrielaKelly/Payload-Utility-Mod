package net.payload.module.modules.client;

import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.CommonPongC2SPacket;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Vec3d;
import net.payload.Payload;
import net.payload.event.events.SendPacketEvent;
import net.payload.event.listeners.SendPacketListener;
import net.payload.module.Category;
import net.payload.module.Module;
import net.payload.settings.types.BooleanSetting;

public class PacketLog extends Module implements SendPacketListener {

    private final BooleanSetting logChat = BooleanSetting.builder()
            .id("packetlogger_logchat")
            .displayName("Log Chat")
            .defaultValue(false)
            .build();

    private final BooleanSetting moveFull = BooleanSetting.builder()
            .id("packetlogger_movefull")
            .displayName("PlayerMove Full")
            .defaultValue(false)
            .build();

    private final BooleanSetting moveLook = BooleanSetting.builder()
            .id("packetlogger_movelook")
            .displayName("PlayerMove Look")
            .defaultValue(false)
            .build();

    private final BooleanSetting movePos = BooleanSetting.builder()
            .id("packetlogger_movepos")
            .displayName("PlayerMove Position")
            .defaultValue(false)
            .build();

    private final BooleanSetting moveGround = BooleanSetting.builder()
            .id("packetlogger_moveground")
            .displayName("PlayerMove Ground")
            .defaultValue(false)
            .build();

    private final BooleanSetting vehicleMove = BooleanSetting.builder()
            .id("packetlogger_vehiclemove")
            .displayName("VehicleMove")
            .defaultValue(false)
            .build();

    private final BooleanSetting playerAction = BooleanSetting.builder()
            .id("packetlogger_playeraction")
            .displayName("PlayerAction")
            .defaultValue(false)
            .build();

    private final BooleanSetting updateSlot = BooleanSetting.builder()
            .id("packetlogger_updateslot")
            .displayName("UpdateSelectedSlot")
            .defaultValue(false)
            .build();

    private final BooleanSetting clickSlot = BooleanSetting.builder()
            .id("packetlogger_clickslot")
            .displayName("ClickSlot")
            .defaultValue(false)
            .build();

    private final BooleanSetting handSwing = BooleanSetting.builder()
            .id("packetlogger_handswing")
            .displayName("HandSwing")
            .defaultValue(false)
            .build();

    private final BooleanSetting interactEntity = BooleanSetting.builder()
            .id("packetlogger_interactentity")
            .displayName("PlayerInteractEntity")
            .defaultValue(false)
            .build();

    private final BooleanSetting interactBlock = BooleanSetting.builder()
            .id("packetlogger_interactblock")
            .displayName("PlayerInteractBlock")
            .defaultValue(false)
            .build();

    private final BooleanSetting interactItem = BooleanSetting.builder()
            .id("packetlogger_interactitem")
            .displayName("PlayerInteractItem")
            .defaultValue(false)
            .build();

    private final BooleanSetting command = BooleanSetting.builder()
            .id("packetlogger_command")
            .displayName("ClientCommand")
            .defaultValue(false)
            .build();

    private final BooleanSetting status = BooleanSetting.builder()
            .id("packetlogger_status")
            .displayName("ClientStatus")
            .defaultValue(false)
            .build();

    private final BooleanSetting closeScreen = BooleanSetting.builder()
            .id("packetlogger_closescreen")
            .displayName("CloseHandledScreen")
            .defaultValue(false)
            .build();

    private final BooleanSetting teleportConfirm = BooleanSetting.builder()
            .id("packetlogger_teleportconfirm")
            .displayName("TeleportConfirm")
            .defaultValue(false)
            .build();

    private final BooleanSetting pong = BooleanSetting.builder()
            .id("packetlogger_pong")
            .displayName("CommonPong")
            .defaultValue(false)
            .build();

    public PacketLog() {
            super("PacketLog");
            this.setCategory(Category.of("Client"));
            this.setDescription("Gives you detailed descriptions of sent packets");

        this.addSetting(moveFull);
        this.addSetting(movePos);
        this.addSetting(moveLook);
        this.addSetting(moveGround);
        this.addSetting(vehicleMove);
        this.addSetting(playerAction);
        this.addSetting(updateSlot);
        this.addSetting(handSwing);
        this.addSetting(clickSlot);
        this.addSetting(interactEntity);
        this.addSetting(interactBlock);
        this.addSetting(interactItem);
        this.addSetting(command);
        this.addSetting(status);
        this.addSetting(closeScreen);
        this.addSetting(teleportConfirm);
        this.addSetting(pong);

    }

        @Override
        public void onDisable() {
            Payload.getInstance().eventManager.RemoveListener(SendPacketListener.class, this);
        }

        @Override
        public void onEnable() {
            Payload.getInstance().eventManager.AddListener(SendPacketListener.class, this);
        }

        @Override
        public void onToggle() {

        }

        private void log(String format, Object... args) {
            String message = String.format(format, args);
            sendChatMessage(message);
        }

    @Override
    public void onSendPacket(SendPacketEvent event) {
        Packet<?> packet = event.GetPacket();

        if (packet instanceof PlayerMoveC2SPacket.Full full && moveFull.get()) {
            log("PlayerMove Full - Pos: %s, Rot: %s, OnGround: %s",
                    new Vec3d(full.getX(0), full.getY(0), full.getZ(0)),
                    String.format("%.2f/%.2f", full.getYaw(0f), full.getPitch(0f)),
                    full.isOnGround());
        }

        if (packet instanceof PlayerMoveC2SPacket.PositionAndOnGround pos && movePos.get()) {
            log("PlayerMove Position - Pos: %s, OnGround: %s",
                    new Vec3d(pos.getX(0), pos.getY(0), pos.getZ(0)),
                    pos.isOnGround());
        }

        if (packet instanceof PlayerMoveC2SPacket.LookAndOnGround look && moveLook.get()) {
            log("PlayerMove Look - Yaw: %.2f, Pitch: %.2f, OnGround: %s",
                    look.getYaw(0f), look.getPitch(0f), look.isOnGround());
        }

        if (packet instanceof PlayerMoveC2SPacket.OnGroundOnly ground && moveGround.get()) {
            log("PlayerMove Ground - OnGround: %s", ground.isOnGround());
        }

        if (packet instanceof VehicleMoveC2SPacket p && vehicleMove.get()) {
            log("VehicleMove - Pos: %s, Yaw: %.2f, Pitch: %.2f",
                    new Vec3d(p.position().getX(), p.position().getY(), p.position().getZ()), p.yaw(), p.pitch());
        }

        if (packet instanceof PlayerActionC2SPacket p && playerAction.get()) {
            log("PlayerAction - Action: %s, Direction: %s, Pos: %s",
                    p.getAction(), p.getDirection(), p.getPos());
        }

        if (packet instanceof UpdateSelectedSlotC2SPacket p && updateSlot.get()) {
            log("UpdateSelectedSlot - Slot: %d", p.getSelectedSlot());
        }

        if (packet instanceof HandSwingC2SPacket p && handSwing.get()) {
            log("HandSwing - Hand: %s", p.getHand());
        }

        if (packet instanceof ClickSlotC2SPacket p && clickSlot.get()) {
            log("ClickSlot - Type: %s, Slot: %d, Button: %d, ID: %d",
                    p.getActionType(), p.getSlot(), p.getButton(), p.getSyncId());
        }

        if (packet instanceof PlayerInteractEntityC2SPacket p && interactEntity.get()) {
            log("InteractEntity");
        }

        if (packet instanceof PlayerInteractBlockC2SPacket p && interactBlock.get()) {
            BlockHitResult hit = p.getBlockHitResult();
            log("InteractBlock - Pos: %s, Side: %s, Hand: %s", hit.getBlockPos(), hit.getSide(), p.getHand());
        }

        if (packet instanceof PlayerInteractItemC2SPacket p && interactItem.get()) {
            log("InteractItem - Hand: %s", p.getHand());
        }

        if (packet instanceof ClientCommandC2SPacket p && command.get()) {
            log("ClientCommand - Mode: %s", p.getMode());
        }

        if (packet instanceof ClientStatusC2SPacket p && status.get()) {
            log("ClientStatus - Mode: %s", p.getMode());
        }

        if (packet instanceof CloseHandledScreenC2SPacket p && closeScreen.get()) {
            log("CloseScreen - ID: %d", p.getSyncId());
        }

        if (packet instanceof TeleportConfirmC2SPacket p && teleportConfirm.get()) {
            log("TeleportConfirm - ID: %d", p.getTeleportId());
        }

        if (packet instanceof CommonPongC2SPacket p && pong.get()) {
            log("Pong - ID: %d", p.getParameter());
        }
    }
}
