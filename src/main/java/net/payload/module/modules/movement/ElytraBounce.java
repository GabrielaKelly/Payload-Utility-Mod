/*
 * Copyright (c) 2014-2024 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.payload.module.modules.movement;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.payload.Payload;
import net.payload.event.events.*;
import net.payload.event.listeners.*;
import net.payload.mixin.interfaces.ILivingEntity;
import net.payload.module.Category;
import net.payload.module.Module;
import net.payload.settings.types.BooleanSetting;
import net.payload.settings.types.FloatSetting;
import net.payload.utils.math.CacheTimer;

public class ElytraBounce extends Module implements TickListener, ReceivePacketListener, SendPacketListener, RotateListener, TravelListener, PlayerMoveEventListener {

    private BooleanSetting instantStart = BooleanSetting.builder()
            .id("elytrabounce_autostart")
            .displayName("AutoStart")
            .description("")
            .defaultValue(true)
            .build();

    private FloatSetting pitch = FloatSetting.builder()
            .id("elytrabounce_pitch")
            .displayName("Pitch")
            .description("")
            .defaultValue(85f)
            .minValue(0f)
            .maxValue(90f)
            .step(0.5f)
            .build();

    private FloatSetting timeout = FloatSetting.builder()
            .id("elytrabounce_timeout")
            .displayName("Timeout")
            .description("")
            .defaultValue(0.5f)
            .minValue(0.1f)
            .maxValue(1f)
            .step(0.1f)
            .build();

    public BooleanSetting sprint = BooleanSetting.builder()
            .id("elytrabounce_sprint")
            .displayName("Sprint")
            .description("")
            .defaultValue(true)
            .build();

    public BooleanSetting autoJump = BooleanSetting.builder()
            .id("elytrabounce_autojump")
            .displayName("AutoJump")
            .description("")
            .defaultValue(true)
            .build();

    private BooleanSetting autoRun = BooleanSetting.builder()
            .id("elytrabounce_autorun")
            .displayName("AutoRun")
            .description("")
            .defaultValue(true)
            .build();

    private BooleanSetting chunkStop = BooleanSetting.builder()
            .id("elytrabounce_chunkstop")
            .displayName("ChunkStop")
            .description("")
            .defaultValue(true)
            .build();

    private BooleanSetting speedCap = BooleanSetting.builder()
            .id("elytrabounce_speedcap")
            .displayName("Speed Cap")
            .description("Cap speed")
            .defaultValue(false)
            .build();

    private FloatSetting speedCapFloat = FloatSetting.builder()
            .id("elytrabounce_speedcapfloat")
            .displayName("Speed Cap km/h")
            .description("Caps your speed")
            .defaultValue(110f)
            .minValue(0f)
            .maxValue(350f)
            .step(5f)
            .build();

    private boolean hasElytra = false;

    boolean rubberbanded = false;

    double prevFov;

    private final CacheTimer instantFlyTimer = new CacheTimer();

    public ElytraBounce() {
        super("ElytraBounce");

        this.setName("ElytraBounce");
        this.setCategory(Category.of("Movement"));
        this.setDescription("Specialized Elytra Fly for quick highway travel");

        this.addSetting(pitch);
        this.addSetting(timeout);
        this.addSetting(autoJump);
        this.addSetting(sprint);
        this.addSetting(autoRun);
        this.addSetting(instantStart);
        this.addSetting(speedCap);
        this.addSetting(speedCapFloat);
        this.addSetting(chunkStop);
    }

    @Override
    public void onDisable() {
        Payload.getInstance().eventManager.RemoveListener(SendPacketListener.class, this);
        Payload.getInstance().eventManager.RemoveListener(ReceivePacketListener.class, this);
        Payload.getInstance().eventManager.RemoveListener(TickListener.class, this);
        Payload.getInstance().eventManager.RemoveListener(TravelListener.class, this);
        Payload.getInstance().eventManager.RemoveListener(RotateListener.class, this);
        Payload.getInstance().eventManager.RemoveListener(PlayerMoveEventListener.class, this);

        MC.options.jumpKey.setPressed(false);
        MC.options.forwardKey.setPressed(false);

        rubberbanded = false;
        if (prevFov != 0 && !sprint.getValue()) MC.options.getFovEffectScale().setValue(prevFov);
    }

    @Override
    public void onEnable() {
        Payload.getInstance().eventManager.AddListener(SendPacketListener.class, this);
        Payload.getInstance().eventManager.AddListener(ReceivePacketListener.class, this);
        Payload.getInstance().eventManager.AddListener(TickListener.class, this);
        Payload.getInstance().eventManager.AddListener(TravelListener.class, this);
        Payload.getInstance().eventManager.AddListener(RotateListener.class, this);
        Payload.getInstance().eventManager.AddListener(PlayerMoveEventListener.class, this);

        prevFov = MC.options.getFovEffectScale().getValue();
    }

    @Override
    public void onToggle() {

    }
    @Override
    public void onReceivePacket(ReceivePacketEvent readPacketEvent) {
        if (nullCheck()) return;

        if (hasElytra && readPacketEvent.getPacket() instanceof PlayerPositionLookS2CPacket) {
            rubberbanded = true;
            MC.player.stopGliding();
        }
    }

    boolean prev;
    float prepitch;

    @Override
    public void onLastRotate(RotateEvent event) {
        if (nullCheck()) return;
        if (hasElytra) {
            event.setPitch(pitch.getValue());
        }
    }

    @Override
    public void onSendPacket(SendPacketEvent event) {
        if (nullCheck()) return;
        if (hasElytra && event.GetPacket() instanceof ClientCommandC2SPacket && ((ClientCommandC2SPacket) event.GetPacket()).getMode().equals(ClientCommandC2SPacket.Mode.START_FALL_FLYING) && !sprint.getValue()) {
            MC.player.setSprinting(true);
        }
    }
    
    @Override
    public void onTick(TickEvent.Pre event) {
        if (nullCheck()) return;

        for (ItemStack is : MC.player.getArmorItems()) {
            if (is.getItem() == Items.ELYTRA) {
                hasElytra = true;
                break;
            } else {
                hasElytra = false;
            }
        }

        ILivingEntity ent = (ILivingEntity) MC.player;

        ent.setJumpCooldown(0);

        if (!MC.player.isGliding()) {
        if (!MC.player.isOnGround() && instantStart.getValue() && MC.player.getVelocity().getY() < 0D) {
            if (!instantFlyTimer.passed((long) (1000 * timeout.getValue()))) return;
            instantFlyTimer.reset();
            MC.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(MC.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
        }
    }

        if (hasElytra) {
            if (autoJump.getValue()) MC.options.jumpKey.setPressed(true);

            PlayerEntity player = MC.player;

            double dx = player.getX() - player.prevX;
            double dz = player.getZ() - player.prevZ;
            double dy = player.getY() - player.prevY;

            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

            double speed = distance * 20 * 3.6;

            if (speedCap.getValue() && speed > speedCapFloat.getValue()) {
                if (autoRun.getValue()) {
                    MC.options.forwardKey.setPressed(false);
                    MC.options.backKey.setPressed(true);
                }
            }
            else if (autoRun.getValue()) {
                MC.options.forwardKey.setPressed(true);
                MC.options.backKey.setPressed(false);
            }

           // if (autoRun.getValue()) MC.options.forwardKey.setPressed(true);
            if (checkConditions(MC.player) && sprint.getValue()) MC.player.setSprinting(true);
        }
    }

    @Override
    public void onTick(TickEvent.Post event) {
        if (nullCheck()) return;
        if (hasElytra) {
            if (!MC.player.isGliding())
                MC.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(MC.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));

            if (checkConditions(MC.player)) {
                if (!sprint.getValue()) {
                    if (MC.player.isGliding()) MC.player.setSprinting(MC.player.isOnGround());
                    else MC.player.setSprinting(true);
                }
            }
        }
    }

    @Override
    public void onTravelPre(TravelEvent.Pre event) {
        if (nullCheck()) return;
        if (hasElytra) {
                prepitch = MC.player.getPitch();
                MC.player.setPitch(pitch.getValue());
                prev = true;
            }
    }

    @Override
    public void onTravelPost(TravelEvent.Post event) {
        if (nullCheck()) return;

        if (prev) {
            prev = false;
            MC.player.setPitch(prepitch);
        }
    }

    public static boolean recastElytra(ClientPlayerEntity player) {
        if (checkConditions(player) && ignoreGround(player)) {
            player.networkHandler.sendPacket(new ClientCommandC2SPacket(player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
            return true;
        } else return false;
    }

    private static boolean ignoreGround(ClientPlayerEntity player) {
        if (!player.isTouchingWater() && !player.hasStatusEffect(StatusEffects.LEVITATION)) {
            ItemStack itemStack = player.getEquippedStack(EquipmentSlot.CHEST);
            if (itemStack.isOf(Items.ELYTRA)) {
                player.startGliding();
                return true;
            } else return false;
        } else return false;
    }

    public static boolean checkConditions(ClientPlayerEntity player) {
        ItemStack itemStack = player.getEquippedStack(EquipmentSlot.CHEST);
        return (!player.getAbilities().flying && !player.hasVehicle() && !player.isClimbing() && itemStack.isOf(Items.ELYTRA));
    }

    @Override
    public void onMove(PlayerMoveEvent event) {
        if (MC.player.getEquippedStack(EquipmentSlot.CHEST).getItem() != Items.ELYTRA) return;

        if (MC.player.isGliding()) {
            int chunkX = (int) ((MC.player.getX()) / 16);
            int chunkZ = (int) ((MC.player.getZ()) / 16);
            if (chunkStop.getValue()) {
                if (!MC.world.getChunkManager().isChunkLoaded(chunkX, chunkZ)) {
                    event.setX(0);
                    event.setY(0);
                    event.setZ(0);
                }
            }
        }
    }
}

