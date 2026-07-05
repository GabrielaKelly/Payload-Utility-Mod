/*
 * Copyright (c) 2014-2024 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.payload.module.modules.movement;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.MovementType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.payload.Payload;
import net.payload.event.events.TickEvent;
import net.payload.event.events.TravelEvent;
import net.payload.event.listeners.TickListener;
import net.payload.event.listeners.TravelListener;
import net.payload.module.Category;
import net.payload.module.Module;
import net.payload.settings.types.BooleanSetting;
import net.payload.settings.types.FloatSetting;
import net.payload.utils.math.CacheTimer;
import net.payload.utils.player.FindItemResult;
import net.payload.utils.player.InvUtils;

import static net.payload.utils.player.MovementUtil.*;

public class ElytraPacket extends Module implements TickListener, TravelListener {

    private BooleanSetting pinstantFly = BooleanSetting.builder()
            .id("elytrapacket_pinstantFly")
            .displayName("Easy Start")
            .description("no weird double-jump needed!")
            .defaultValue(true)
            .build();

    private BooleanSetting pstandSpoof = BooleanSetting.builder()
            .id("elytrapacket_pstandspoof")
            .displayName("Easy Land")
            .description("Spoofs pose")
            .defaultValue(true)
            .build();

    private BooleanSetting pautoFirework = BooleanSetting.builder()
            .id("elytrapacket_pautoFirework")
            .displayName("Auto Firework")
            .description("Automatically uses fireworks")
            .defaultValue(false)
            .build();

    private FloatSetting pFireworkDelay = FloatSetting.builder()
            .id("elytrapacket_firework_delay")
            .displayName("Firework Delay")
            .description("Time to wait before firework")
            .defaultValue(4f)
            .minValue(1f)
            .maxValue(20f)
            .step(1f)
            .build();

    private FloatSetting sneakDownSpeed = FloatSetting.builder()
            .id("elytrapacket_sneakdownspeed")
            .displayName("DownSpeed")
            .description("")
            .defaultValue(1f)
            .minValue(0.1f)
            .maxValue(5f)
            .step(0.1f)
            .build();

    private FloatSetting upFactor = FloatSetting.builder()
            .id("elytrapacket_upfactor")
            .displayName("UpFactor")
            .description("")
            .defaultValue(1f)
            .minValue(0f)
            .maxValue(5f)
            .step(0.1f)
            .build();


    private BooleanSetting useUpPitch = BooleanSetting.builder()
            .id("elytrapacket_uppitchtoggle")
            .displayName("UpPitch Toggle")
            .description("")
            .defaultValue(false)
            .build();


    private FloatSetting upPitch = FloatSetting.builder()
            .id("elytrapacket_uppitch")
            .displayName("UpPitch")
            .description("")
            .defaultValue(40f)
            .minValue(0f)
            .maxValue(90f)
            .step(0.5f)
            .build();

    private FloatSetting downFactor = FloatSetting.builder()
            .id("elytrapacket_downfactor")
            .displayName("FallSpeed")
            .description("")
            .defaultValue(0f)
            .minValue(-1f)
            .maxValue(3f)
            .step(0.1f)
            .build();

    private FloatSetting speed = FloatSetting.builder()
            .id("elytrapacket_speed")
            .displayName("Speed")
            .description("")
            .defaultValue(1f)
            .minValue(0.1f)
            .maxValue(5f)
            .step(0.1f)
            .build();

    private FloatSetting maxSpeed = FloatSetting.builder()
            .id("elytrapacket_maxspeed")
            .displayName("MaxSpeed")
            .description("")
            .defaultValue(2.5f)
            .minValue(0.1f)
            .maxValue(10.0f)
            .step(0.1f)
            .build();

    private BooleanSetting noDrag = BooleanSetting.builder()
            .id("elytrapacket_nodrag")
            .displayName("NoDrag")
            .description("")
            .defaultValue(true)
            .build();

    private BooleanSetting speedLimit = BooleanSetting.builder()
            .id("elytrapacket_speedlimit")
            .displayName("SpeedLimit")
            .description("")
            .defaultValue(true)
            .build();


    protected double ticksLeft;
    private boolean fireworkTrigger = true;
    private boolean onGround = false;

    private final CacheTimer instantFlyTimer = new CacheTimer();

    protected final Vec3d getRotationVector(float pitch, float yaw) {
        float f = pitch * 0.017453292F;
        float g = -yaw * 0.017453292F;
        float h = MathHelper.cos(g);
        float i = MathHelper.sin(g);
        float j = MathHelper.cos(f);
        float k = MathHelper.sin(f);
        return new Vec3d(i * j, -k, h * j);
    }

    public final Vec3d getRotationVec(float tickDelta) {
        return this.getRotationVector(-upPitch.getValue(), MC.player.getYaw(tickDelta));
    }


    public ElytraPacket() {
        super("PacketElytra");

        this.setName("PacketElytra");
        this.setCategory(Category.of("Movement"));
        this.setDescription("Gives the player creative-like elytra flight");

        this.addSetting(pinstantFly);
        this.addSetting(pstandSpoof);
        this.addSetting(speed);
        this.addSetting(speedLimit);
        this.addSetting(maxSpeed);
        this.addSetting(sneakDownSpeed);
        this.addSetting(upFactor);
        this.addSetting(useUpPitch);
        this.addSetting(upPitch);
        this.addSetting(downFactor);
        this.addSetting(noDrag);
        this.addSetting(pautoFirework);
        this.addSetting(pFireworkDelay);
    }

    @Override
    public void onDisable() {
        Payload.getInstance().eventManager.RemoveListener(TravelListener.class, this);
        Payload.getInstance().eventManager.RemoveListener(TickListener.class, this);
    }

    @Override
    public void onEnable() {
        Payload.getInstance().eventManager.AddListener(TravelListener.class, this);
        Payload.getInstance().eventManager.AddListener(TickListener.class, this);
        fireworkTrigger = true;

        instantFlyTimer.reset();

        if (Payload.getInstance().moduleManager.elytraPlus.state.getValue()) {
            Payload.getInstance().moduleManager.elytraPlus.toggle();
        }
    }

    @Override
    public void onToggle() {
    }

    @Override
    public void onTick(TickEvent.Pre event) {
        if (nullCheck()) return;

        if (!MC.player.isGliding() && pautoFirework.getValue()) {
            fireworkTrigger = true;
        }

        if (fireworkTrigger) {
            instaUseFirework();
            ticksLeft = lagFireworkFix() * 20;
        }

        if (pautoFirework.getValue() && MC.player.isGliding()) {
            useFirework();
        }

        onGround = MC.player.isOnGround();

        if (onGround) {
            instantFlyTimer.reset();
        }

        if (hasElytra() && MC.options.jumpKey.isPressed() && !MC.player.isGliding() && !onGround && pinstantFly.getValue() && MC.player.getVelocity().getY() < 0D) {
            if (!instantFlyTimer.passed((100))) return;
            if (instantFlyTimer.passed(1000)) return;
            MC.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(MC.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
        }
    }

    @Override
    public void onTick(TickEvent.Post event) {

    }

    public boolean hasElytra() {
        if (nullCheck()) return false;

        ItemStack chest = MC.player.getEquippedStack(EquipmentSlot.CHEST);

        if (chest.getItem() != Items.ELYTRA) {
            return false;
        }

        return true;

    }

    private void instaUseFirework() {

        if (!MC.player.isGliding() || !pautoFirework.getValue()) {
            return;
        }

        FindItemResult itemResult = InvUtils.findInHotbar(Items.FIREWORK_ROCKET);

        if (!itemResult.found()) return;

        if (itemResult.isOffhand()) {
            MC.interactionManager.interactItem(MC.player, Hand.OFF_HAND);
            MC.player.swingHand(Hand.OFF_HAND);
        } else {
            InvUtils.swap(itemResult.slot(), true);

            MC.interactionManager.interactItem(MC.player, Hand.MAIN_HAND);
            MC.player.swingHand(Hand.MAIN_HAND);

            InvUtils.swapBack();
        }

        fireworkTrigger = false;
    }

    private void useFirework() {
        if (ticksLeft <= 0) {
            ticksLeft = lagFireworkFix() * 20;

            FindItemResult itemResult = InvUtils.findInHotbar(Items.FIREWORK_ROCKET);

            if (!itemResult.found()) return;

            if (itemResult.isOffhand()) {
                MC.interactionManager.interactItem(MC.player, Hand.OFF_HAND);
                MC.player.swingHand(Hand.OFF_HAND);
            } else {
                InvUtils.swap(itemResult.slot(), true);

                MC.interactionManager.interactItem(MC.player, Hand.MAIN_HAND);
                MC.player.swingHand(Hand.MAIN_HAND);

                InvUtils.swapBack();
            }
        }
        ticksLeft--;
    }


    private double getX() {
        return getMotionX();
    }

    private void setX(double f) {
        setMotionX(f);
    }

    private double getY() {
        return getMotionY();
    }

    private void setY(double f) {
        setMotionY(f);
    }

    private double getZ() {
        return getMotionZ();
    }

    private void setZ(double f) {
        setMotionZ(f);
    }

    public boolean canPacketEfly() {
        return this.state.getValue() && (MC.player.getEquippedStack(EquipmentSlot.CHEST).getItem() == Items.ELYTRA) && !MC.player.isOnGround() && pstandSpoof.getValue();
    }

    @Override
    public void onTravelPre(TravelEvent.Pre event) {

        if (nullCheck() || !hasElytra() || !MC.player.isGliding()) return;

        if (!useUpPitch.getValue()) {
            if (!(MC.options.sneakKey.isPressed() && MC.options.jumpKey.isPressed())) {
                if (MC.options.sneakKey.isPressed()) {
                    setY(-sneakDownSpeed.getValue());
                }
                else if (MC.options.jumpKey.isPressed()) {
                    setY(upFactor.getValue());
                } else {
                    setY(-downFactor.getValue());
                }
            } else {
                setY(0);
            }

            double[] dir = directionSpeedKey(speed.getValue());
            setX(dir[0]);
            setZ(dir[1]);


            if (!noDrag.getValue()) {
                setY(getY() * 0.9900000095367432D);
                setX(getX() * 0.9800000190734863D);
                setZ(getZ() * 0.9900000095367432D);
            }

            double finalDist = Math.sqrt(getX() * getX() + getZ() * getZ());

            if (speedLimit.getValue() && finalDist > maxSpeed.getValue()) {
                setX(getX() * maxSpeed.getValue() / finalDist);
                setZ(getZ() * maxSpeed.getValue() / finalDist);
            }

        } else {
            Vec3d lookVec = getRotationVec(MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(false));
            double lookDist = Math.sqrt(lookVec.x * lookVec.x + lookVec.z * lookVec.z);
            double motionDist = Math.sqrt(getX() * getX() + getZ() * getZ());
            if (MC.options.sneakKey.isPressed()) {
                setY(-sneakDownSpeed.getValue());
            }
            else if (MC.options.jumpKey.isPressed()) {
                if (motionDist > upFactor.getValue() / 90) {
                    double rawUpSpeed = motionDist * 0.01325D;
                    setY(getY() + rawUpSpeed * 3.2D);
                    setX(getX() - lookVec.x * rawUpSpeed / lookDist);
                    setZ(getZ() - lookVec.z * rawUpSpeed / lookDist);
                } else {
                    double[] dir = directionSpeedKey(speed.getValue());
                    setX(dir[0]);
                    setZ(dir[1]);
                }
            }
            else {
                setY(-downFactor.getValue());
            }

            if (lookDist > 0.0D) {
                setX(getX() + (lookVec.x / lookDist * motionDist - getX()) * 0.1D);
                setZ(getZ() + (lookVec.z / lookDist * motionDist - getZ()) * 0.1D);
            }
            if (!MC.options.jumpKey.isPressed()) {
                double[] dir = directionSpeedKey(speed.getValue());
                setX(dir[0]);
                setZ(dir[1]);
            }
            if (!noDrag.getValue()) {
                setY(getY() * 0.9900000095367432D);
                setX(getX() * 0.9800000190734863D);
                setZ(getZ() * 0.9900000095367432D);
            }
            double finalDist = Math.sqrt(getX() * getX() + getZ() * getZ());
            if (speedLimit.getValue() && finalDist > maxSpeed.getValue()) {
                setX(getX() * maxSpeed.getValue() / finalDist);
                setZ(getZ() * maxSpeed.getValue() / finalDist);
            }
            event.cancel();
            MC.player.move(MovementType.SELF, MC.player.getVelocity());
        }
    }

    @Override
    public void onTravelPost(TravelEvent.Post event) {

    }

    private float lagFireworkFix() {
        if (pFireworkDelay.getValue() == 4) {
            return 3.93f;
        }
        return pFireworkDelay.getValue();
    }
}


